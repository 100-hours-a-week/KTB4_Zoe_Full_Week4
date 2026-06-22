package kr.adapterz.springboot.service;

import jakarta.servlet.http.HttpServletRequest;
import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.auth.ForbiddenException;
import kr.adapterz.springboot.auth.UnauthorizedException;
import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.PostCreateResponseDto;
import kr.adapterz.springboot.dto.PostDetailResponseDto;
import kr.adapterz.springboot.dto.PostListResponseDto;
import kr.adapterz.springboot.dto.PostRequestDto;
import kr.adapterz.springboot.dto.PostSummaryResponseDto;
import kr.adapterz.springboot.dto.PostUpdateResponseDto;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.PostView;
import kr.adapterz.springboot.entity.PostVersion;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.PostRateLimitExceededException;
import kr.adapterz.springboot.repository.CommentRepository;
import kr.adapterz.springboot.repository.LikeRepository;
import kr.adapterz.springboot.repository.PostRepository;
import kr.adapterz.springboot.repository.PostViewRepository;
import kr.adapterz.springboot.repository.PostVersionRepository;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final long MAX_POSTS_PER_MINUTE = 3L;
    private static final long POST_LIMIT_WINDOW_MINUTES = 1L;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final PostVersionRepository postVersionRepository;
    private final PostViewRepository postViewRepository;
    private final HttpServletRequest request;

    @Transactional
    public PostCreateResponseDto createPost(PostRequestDto request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        validatePostRateLimit(currentUserId);

        Post post = new Post(
                request.getTitle(),
                request.getContent(),
                author
        );
        post.replaceImages(request.getImageUrls());

        Post savedPost = postRepository.save(post);

        return new PostCreateResponseDto(savedPost);
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<PostListResponseDto> getPosts() {
        List<PostSummaryResponseDto> posts = postRepository.findAll().stream()
                .map(this::toSummaryResponse)
                .toList();

        return new ApiResponseDto<>("fetch_success", new PostListResponseDto(posts));
    }

    @Transactional
    public PostDetailResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Long currentUserId = getCurrentUserIdOrNull();
        increaseViewCountIfAllowed(post, currentUserId);

        return toDetailResponse(post, currentUserId);
    }

    @Transactional
    public PostUpdateResponseDto updatePost(Long postId, PostRequestDto request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));


        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("게시글 수정 권한이 없습니다.");
        }

        int nextVersion = postVersionRepository.findLastVersionNumber(post.getId()) + 1;
        postVersionRepository.save(new PostVersion(
                post,
                post.getAuthor(),
                post.getTitle(),
                post.getContent(),
                nextVersion
        ));

        post.changeTitle(request.getTitle());
        post.changeContent(request.getContent());
        post.replaceImages(request.getImageUrls());

        return new PostUpdateResponseDto(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));


        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("게시글 삭제 권한이 없습니다.");
        }

        post.delete();
        postRepository.save(post);
    }

    private PostDetailResponseDto toDetailResponse(Post post, Long currentUserId) {
        long commentCount = commentRepository.countByPostId(post.getId());
        long likeCount = likeRepository.countByPostId(post.getId());
        boolean liked = currentUserId != null
                && likeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        boolean edited = postVersionRepository.existsByPostId(post.getId());

        return new PostDetailResponseDto(post, commentCount, likeCount, liked, edited);
    }

    private PostSummaryResponseDto toSummaryResponse(Post post) {
        long commentCount = commentRepository.countByPostId(post.getId());
        long likeCount = likeRepository.countByPostId(post.getId());
        boolean edited = postVersionRepository.existsByPostId(post.getId());

        return new PostSummaryResponseDto(post, likeCount, commentCount, edited);
    }

    private void validatePostRateLimit(Long currentUserId) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(POST_LIMIT_WINDOW_MINUTES);
        long recentPostCount = postRepository.countByAuthorIdAndCreatedAtAfter(currentUserId, windowStart);

        if (recentPostCount >= MAX_POSTS_PER_MINUTE) {
            throw new PostRateLimitExceededException();
        }
    }

    private Long getCurrentUserIdOrNull() {
        try {
            Long currentUserId = currentUserProvider.getCurrentUserId();
            return userRepository.findById(currentUserId)
                    .filter(user -> !user.isDeleted())
                    .map(User::getId)
                    .orElse(null);
        } catch (UnauthorizedException e) {
            return null;
        }
    }

    private void increaseViewCountIfAllowed(Post post, Long currentUserId) {
        LocalDateTime now = LocalDateTime.now();
        User viewer = findViewer(currentUserId);
        String viewerKey = createViewerKey(currentUserId);

        PostView postView = postViewRepository.findByPostIdAndViewerKey(post.getId(), viewerKey)
                .orElse(null);

        if (postView == null) {
            post.increaseViewCount();
            postViewRepository.save(new PostView(post, viewer, viewerKey, now));
            return;
        }

        if (postView.canIncreaseViewCount(now)) {
            post.increaseViewCount();
            postView.updateLastViewedAt(now);
        }
    }

    private User findViewer(Long currentUserId) {
        if (currentUserId == null) {
            return null;
        }

        return userRepository.findById(currentUserId)
                .filter(user -> !user.isDeleted())
                .orElse(null);
    }

    private String createViewerKey(Long currentUserId) {
        if (currentUserId != null) {
            return "USER:" + currentUserId;
        }

        String userAgent = Objects.toString(request.getHeader("User-Agent"), "");
        return "GUEST:" + getClientIp() + ":" + Integer.toHexString(userAgent.hashCode());
    }

    private String getClientIp() {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
