package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Like;

public interface LikeRepository {

    Like save(Like like);

    boolean existsByPostIdAndUserId(Long postId, Long UserId);

    boolean deleteByPostIdAndUserId(Long postId, Long UserId);

    long countByPostId(Long postId);
}
