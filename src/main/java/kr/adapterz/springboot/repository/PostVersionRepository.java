package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.PostVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PostVersionRepository extends JpaRepository<PostVersion, Long> {

    boolean existsByPostId(Long postId);

    @Query("select coalesce(max(pv.postVersion), 0) from PostVersion pv where pv.post.id = :postId")
    int findLastVersionNumber(@Param("postId") Long postId);

    @Query("""
            select distinct pv.post.id
            from PostVersion pv
            where pv.post.id in :postIds
            """)
    Set<Long> findEditedPostIds(@Param("postIds") List<Long> postIds);
}
