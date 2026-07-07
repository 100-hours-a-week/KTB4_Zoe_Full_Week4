package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    @Query("""
            select pi.post.id as postId, pi.imageUrl as imageUrl
            from PostImage pi
            where pi.post.id in :postIds
              and pi.imageOrder = 0
            """)
    List<PostThumbnailProjection> findThumbnailsByPostIdIn(@Param("postIds") List<Long> postIds);
}
