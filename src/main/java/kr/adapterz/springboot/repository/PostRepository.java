package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.PostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    long countByAuthorIdAndCreatedAtAfter(Long authorId, LocalDateTime createdAt);

    @Query("""
            select p.id
            from Post p
            where p.status <> :status
              and (:cursor is null or p.id < :cursor)
            order by p.id desc
            """)
    List<Long> findPostIdsByCursor(
            @Param("status") PostStatus status,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            select p
            from Post p
            join fetch p.author
            where p.id in :postIds
            order by p.id desc
            """)
    List<Post> findAllByIdInWithAuthor(@Param("postIds") List<Long> postIds);
}
