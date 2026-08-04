package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.PollVote;
import kr.adapterz.springboot.entity.PollVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PollVoteRepository extends
        JpaRepository<PollVote, PollVoteId>,
        PollVotePersistRepository {

    long countByIdPollId(Long pollId);

    @Query("""
            select pv.id.pollId as pollId, pv.optionId as optionId, count(pv) as voteCount
            from PollVote pv
            where pv.id.pollId = :pollId
            group by pv.id.pollId, pv.optionId
            """)
    List<PollVoteCountProjection> countVotesByOption(@Param("pollId") Long pollId);

    List<PollVote> findAllByIdUserIdAndIdPollIdIn(Long userId, List<Long> pollIds);

    @Query("""
            select pv.id.pollId as pollId, count(pv) as totalVoteCount
            from PollVote pv
            where pv.id.pollId in :pollIds
            group by pv.id.pollId
            """)
    List<PollTotalVoteCountProjection> countTotalVotesByPollIds(@Param("pollIds") List<Long> pollIds);

    @Query("""
            select pv.id.pollId as pollId, pv.optionId as optionId, count(pv) as voteCount
            from PollVote pv
            where pv.id.pollId in :pollIds
            group by pv.id.pollId, pv.optionId
            """)
    List<PollVoteCountProjection> countVotesByPollIds(@Param("pollIds") List<Long> pollIds);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO poll_votes (
                poll_id,
                user_id,
                option_id,
                created_at,
                updated_at
            ) VALUES (
                :pollId,
                :userId,
                :optionId,
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            ) AS new_vote
            ON DUPLICATE KEY UPDATE
                option_id = new_vote.option_id,
                updated_at = new_vote.updated_at
            """, nativeQuery = true)
    int upsertVote(
            @Param("pollId") Long pollId,
            @Param("userId") Long userId,
            @Param("optionId") Long optionId
    );

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from PollVote pv
            where pv.id.pollId = :pollId
              and pv.id.userId = :userId
            """)
    int deleteCurrentVote(
            @Param("pollId") Long pollId,
            @Param("userId") Long userId
    );
}
