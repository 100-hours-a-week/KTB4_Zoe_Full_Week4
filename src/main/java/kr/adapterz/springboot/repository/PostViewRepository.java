package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostViewRepository extends JpaRepository<PostView, Long> {

    Optional<PostView> findByPostIdAndViewerKey(Long postId, String viewerKey);
}
