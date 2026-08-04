package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.PollVote;
import kr.adapterz.springboot.entity.PollVoteId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollVoteRepository extends
        JpaRepository<PollVote, PollVoteId>,
        PollVotePersistRepository {
}
