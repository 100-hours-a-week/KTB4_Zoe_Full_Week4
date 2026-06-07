package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Comment;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCommentRepository implements CommentRepository {

    private final AtomicLong sequence = new AtomicLong(1L);

    private final ConcurrentMap<Long, Comment> comments = new ConcurrentHashMap<>();

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            comment.assignId(sequence.getAndIncrement());
        }

        comments.put(comment.getId(), comment);
        return comment;
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(comments.get(id));
    }

    @Override
    public List<Comment> findByPostId(Long postId) {
        return comments.values().stream()
                .filter(comment -> comment.getPost().getId().equals(postId))
                .toList();
    }

    @Override
    public boolean deleteById(Long id) {
        return comments.remove(id) != null;
    }

    @Override
    public long countByPostId(Long postId) {
        return comments.values().stream()
                .filter(comment -> comment.getPost().getId().equals(postId))
                .count();
    }
}