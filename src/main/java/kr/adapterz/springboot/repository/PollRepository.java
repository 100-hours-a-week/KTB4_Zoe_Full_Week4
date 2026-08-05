package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Poll;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PollRepository extends JpaRepository<Poll, Long> {

    @Query("""
            select distinct p
            from Poll p
            left join fetch p.options
            where p.postId = :postId
            """)
    Optional<Poll> findByPostIdWithOptions(@Param("postId") Long postId);

    @Query("""
            select distinct p
            from Poll p
            left join fetch p.options
            where p.postId in :postIds
            """)
    List<Poll> findAllByPostIdsWithOptions(@Param("postIds") List<Long> postIds);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("""
            select p
            from Poll p
            where p.postId = :postId
            """)
    Optional<Poll> findByPostIdForParticipation(@Param("postId") Long postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from Poll p
            where p.postId = :postId
            """)
    Optional<Poll> findByPostIdForUpdate(@Param("postId") Long postId);
}
