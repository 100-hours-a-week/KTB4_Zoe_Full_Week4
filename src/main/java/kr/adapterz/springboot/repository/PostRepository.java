package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    long countByAuthorIdAndCreatedAtAfter(Long authorId, LocalDateTime createdAt);

    @Query("""
            select distinct p
            from Post p
            join fetch p.author
            left join fetch p.images
            where p.status <> :status
            order by p.createdAt desc
            """)
    List<Post> findAllByStatusNotWithAuthorAndImages(@Param("status") PostStatus status);
}
