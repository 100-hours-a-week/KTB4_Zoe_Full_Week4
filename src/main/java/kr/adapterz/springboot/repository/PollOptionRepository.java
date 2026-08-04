package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {

    Optional<PollOption> findByIdAndPollPostId(Long optionId, Long pollId);
}
