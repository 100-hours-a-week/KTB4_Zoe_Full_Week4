package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.LikeResponseDto;
import kr.adapterz.springboot.entity.Like;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.DuplicatePostLikeException;
import kr.adapterz.springboot.exception.PostLikeNotFoundException;
import kr.adapterz.springboot.exception.PostNotFoundException;
import kr.adapterz.springboot.exception.UserNotFoundException;
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
                .orElseThrow(PostNotFoundException::new);

        User user = userRepository.findById(currentUserId)
                .orElseThrow(UserNotFoundException::new);

        if (likeRepository.existsByPostIdAndUserId(postId, currentUserId)) {
            throw new DuplicatePostLikeException();
        }

        likeRepository.save(new Like(post, user));

        long likeCount = likeRepository.countByPostId(postId);

        return new LikeResponseDto(postId, likeCount, true);
    }

    @Transactional
    public LikeResponseDto unlikePost(Long postId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        long deletedCount = likeRepository.deleteByPostIdAndUserId(postId, currentUserId);

        if (deletedCount == 0) {
            throw new PostLikeNotFoundException();
        }

        long likeCount = likeRepository.countByPostId(postId);

        return new LikeResponseDto(postId, likeCount, false);
    }

    @Transactional(readOnly = true)
    public LikeResponseDto getPostLike(Long postId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        long likeCount = likeRepository.countByPostId(postId);
        boolean liked = likeRepository.existsByPostIdAndUserId(postId, currentUserId);

        return new LikeResponseDto(postId, likeCount, liked);
    }
}
