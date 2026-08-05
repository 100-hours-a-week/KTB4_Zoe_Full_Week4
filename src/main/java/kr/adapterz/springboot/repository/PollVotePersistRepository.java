package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.PollVote;

public interface PollVotePersistRepository {

    void persist(PollVote pollVote);
}
