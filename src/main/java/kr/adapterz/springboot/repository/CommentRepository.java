package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = "author")
    List<Comment> findByPostId(Long postId);

    long countByPostId(Long postId);

    @Query("""
            select c.post.id as postId, count(c) as countValue
            from Comment c
            where c.post.id in :postIds
            group by c.post.id
            """)
    List<PostCountProjection> countByPostIdIn(@Param("postIds") List<Long> postIds);
}
