package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Post;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPostRepository implements PostRepository {

    private final AtomicLong sequence = new AtomicLong(1L);

    private final ConcurrentMap<Long, Post> posts = new ConcurrentHashMap<>();

    @Override
    public Post save(Post post) {
        if (post.getId() == null) {
            post.assignId(sequence.getAndIncrement());
        }

        posts.put(post.getId(), post);
        return post;
    }

    @Override
    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(posts.get(id));
    }

    @Override
    public List<Post> findAll() {
        return new ArrayList<>(posts.values());
    }

    @Override
    public boolean deleteById(Long id) {
        return posts.remove(id) != null;
    }
}