package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.PostDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostDraftRepository extends JpaRepository<PostDraft, Long> {

    Optional<PostDraft> findByUserIdAndDraftKey(Long userId, String draftKey);
}
