package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    List<Comment> findByPostId(Long postId);

    boolean deleteById(Long id);
}
