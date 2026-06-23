package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Like;
import kr.adapterz.springboot.entity.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    boolean existsByPostIdAndUserId(Long postId, Long UserId);

    long deleteByPostIdAndUserId(Long postId, Long UserId);

    long countByPostId(Long postId);

    @Query("""
            select l.post.id as postId, count(l) as countValue
            from Like l
            where l.post.id in :postIds
            group by l.post.id
            """)
    List<PostCountProjection> countByPostIdIn(@Param("postIds") List<Long> postIds);
}
