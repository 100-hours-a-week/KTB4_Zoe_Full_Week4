package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {

    Optional<PollOption> findByIdAndPollPostId(Long optionId, Long pollId);

    @Query("""
            select option.id
            from PollOption option
            where option.poll.postId = :pollId
            """)
    Set<Long> findIdsByPollPostId(@Param("pollId") Long pollId);

    @Modifying(flushAutomatically = true)
    @Query("""
            update PollOption option
            set option.optionOrder = option.optionOrder + 1000
            where option.poll.postId = :pollId
            """)
    int shiftOptionOrders(@Param("pollId") Long pollId);
}
