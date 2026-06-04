package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;

import java.util.*;

public class PostRepository {
    private final Map<Long, Post> store = new HashMap<>();
    private Long sequence = 1L;


    public Post save(Post post) {
        post.assignId(sequence++);
        store.put(post.getId(), post);
        return post;
    }

    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Post> findAll() {
        return new ArrayList<>(store.values());
    }
}
