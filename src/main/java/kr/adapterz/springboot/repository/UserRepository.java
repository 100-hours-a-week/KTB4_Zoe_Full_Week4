package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.User;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserRepository {
    private final Map<Long, User> store = new HashMap<>();
    private Long sequence = 1L;

    public User save(User user) {
        user.assignId(sequence++);
        store.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<User> deleteById(Long id) {
        return Optional.ofNullable(store.remove(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }
}
