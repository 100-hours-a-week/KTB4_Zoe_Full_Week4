package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
