package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(Long id);

    List<Post> findAll();

    boolean deleteById(Long id);
}