package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.PostDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostDraftRepository extends JpaRepository<PostDraft, Long> {

    Optional<PostDraft> findByUserIdAndDraftKey(Long userId, String draftKey);

    @Modifying
    @Query("delete from PostDraft pd where pd.post.id = :postId")
    int deleteAllByPostId(@Param("postId") Long postId);
}
