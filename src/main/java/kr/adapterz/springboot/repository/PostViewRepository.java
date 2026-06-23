package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostViewRepository extends JpaRepository<PostView, Long> {

    Optional<PostView> findByPostIdAndViewerKey(Long postId, String viewerKey);

    @Modifying
    @Query("delete from PostView pv where pv.post.id = :postId")
    int deleteAllByPostId(@Param("postId") Long postId);
}
