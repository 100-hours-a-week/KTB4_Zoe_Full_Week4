package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.PollVote;
import kr.adapterz.springboot.entity.PollVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PollVoteRepository extends
        JpaRepository<PollVote, PollVoteId>,
        PollVotePersistRepository {

    long countByIdPollId(Long pollId);

    @Query("""
            select pv.optionId as optionId, count(pv) as voteCount
            from PollVote pv
            where pv.id.pollId = :pollId
            group by pv.optionId
            """)
    List<PollVoteCountProjection> countVotesByOption(@Param("pollId") Long pollId);
}
