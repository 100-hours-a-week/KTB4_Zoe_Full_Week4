package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.auth.ForbiddenException;
import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.PostListResponseDto;
import kr.adapterz.springboot.dto.PostRequestDto;
import kr.adapterz.springboot.dto.PostResponseDto;
import kr.adapterz.springboot.dto.PostSummaryResponseDto;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.repository.CommentRepository;
import kr.adapterz.springboot.repository.LikeRepository;
import kr.adapterz.springboot.repository.PostRepository;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public PostResponseDto createPost(PostRequestDto request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Post post = new Post(
                request.getTitle(),
                request.getContent(),
                request.getImageUrl(),
                author
        );

        Post savedPost = postRepository.save(post);

        return toResponse(savedPost, currentUserId);
    }

    public ApiResponseDto<PostListResponseDto> getPosts() {
        List<PostSummaryResponseDto> posts = postRepository.findAll().stream()
                .map(this::toSummaryResponse)
                .toList();

        return new ApiResponseDto<>("fetch_success", new PostListResponseDto(posts));
    }

    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.increaseViewCount();

        Long currentUserId = currentUserProvider.getCurrentUserId();

        return toResponse(post, currentUserId);
    }

    public PostResponseDto updatePost(Long postId, PostRequestDto request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));


        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("게시글 수정 권한이 없습니다.");
        }

        post.changeTitle(request.getTitle());
        post.changeContent(request.getContent());
        post.changeImageUrl(request.getImageUrl());

        return toResponse(post, currentUserId);
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));


        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("게시글 삭제 권한이 없습니다.");
        }

        boolean deleted = postRepository.deleteById(postId);

        if (!deleted) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }
    }

    private PostResponseDto toResponse(Post post, Long currentUserId) {
        long commentCount = commentRepository.countByPostId(post.getId());
        long likeCount = likeRepository.countByPostId(post.getId());
        boolean liked = currentUserId != null
                && likeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);

        return new PostResponseDto(post, commentCount, likeCount, liked);
    }

    private PostSummaryResponseDto toSummaryResponse(Post post) {
        long commentCount = commentRepository.countByPostId(post.getId());
        long likeCount = likeRepository.countByPostId(post.getId());

        return new PostSummaryResponseDto(post, likeCount, commentCount);
    }
}
