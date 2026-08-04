package kr.adapterz.springboot.repository;

import jakarta.persistence.EntityManager;
import kr.adapterz.springboot.entity.PollVote;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PollVotePersistRepositoryImpl implements PollVotePersistRepository {

    private final EntityManager entityManager;

    @Override
    public void persist(PollVote pollVote) {
        entityManager.persist(pollVote);
    }
}
