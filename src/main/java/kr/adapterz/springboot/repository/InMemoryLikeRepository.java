package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Like;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryLikeRepository implements LikeRepository {

    private final AtomicLong sequence = new AtomicLong(1L);

    private final ConcurrentMap<Long, Like> likes = new ConcurrentHashMap<>();

    @Override
    public Like save(Like like) {
        if (like.getId() == null) {
            like.assignId(sequence.getAndIncrement());
        }

        likes.put(like.getId(), like);
        return like;
    }

    @Override
    public boolean existsByPostIdAndUserId(Long postId, Long userId) {
        return likes.values().stream()
                .anyMatch(like ->
                        like.getPost().getId().equals(postId)
                                && like.getUser().getId().equals(userId)
                );
    }

    @Override
    public boolean deleteByPostIdAndUserId(Long postId, Long userId) {
        Long likeId = likes.values().stream()
                .filter(like ->
                        like.getPost().getId().equals(postId)
                                && like.getUser().getId().equals(userId)
                )
                .map(Like::getId)
                .findFirst()
                .orElse(null);

        if (likeId == null) {
            return false;
        }

        likes.remove(likeId);
        return true;
    }

    @Override
    public long countByPostId(Long postId) {
        return likes.values().stream()
                .filter(like -> like.getPost().getId().equals(postId))
                .count();
    }
}