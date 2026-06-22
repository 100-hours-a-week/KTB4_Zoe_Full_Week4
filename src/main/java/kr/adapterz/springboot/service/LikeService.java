package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.LikeResponseDto;
import kr.adapterz.springboot.entity.Like;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.repository.LikeRepository;
import kr.adapterz.springboot.repository.PostRepository;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public LikeResponseDto likePost(Long postId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (likeRepository.existsByPostIdAndUserId(postId, currentUserId)) {
            throw new IllegalArgumentException("이미 좋아요를 누른 게시글입니다.");
        }

        likeRepository.save(new Like(post, user));

        long likeCount = likeRepository.countByPostId(postId);

        return new LikeResponseDto(postId, likeCount, true);
    }

    @Transactional
    public LikeResponseDto unlikePost(Long postId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        long deletedCount = likeRepository.deleteByPostIdAndUserId(postId, currentUserId);

        if (deletedCount == 0) {
            throw new IllegalArgumentException("좋아요를 누르지 않은 게시글입니다.");
        }

        long likeCount = likeRepository.countByPostId(postId);

        return new LikeResponseDto(postId, likeCount, false);
    }

    @Transactional(readOnly = true)
    public LikeResponseDto getPostLike(Long postId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        long likeCount = likeRepository.countByPostId(postId);
        boolean liked = likeRepository.existsByPostIdAndUserId(postId, currentUserId);

        return new LikeResponseDto(postId, likeCount, liked);
    }
}
