package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Like;
import kr.adapterz.springboot.entity.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    boolean existsByPostIdAndUserId(Long postId, Long UserId);

    long deleteByPostIdAndUserId(Long postId, Long UserId);

    long countByPostId(Long postId);
}
