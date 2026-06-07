package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final AtomicLong sequence = new AtomicLong(1L);

    private final ConcurrentMap<Long, User> users = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Long> emailIndex = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        Long id = user.getId();

        if (id == null) {
            id = sequence.getAndIncrement();
        }

        User savedUser = new User(
                id,
                user.getEmail(),
                user.getPassword(),
                user.getNickname()
        );

        users.put(id, savedUser);
        emailIndex.put(savedUser.getEmail(), id);

        return savedUser;
    }

    //user가 없을 수도 있기때문에 Optional
    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Long userId = emailIndex.get(email);

        if (userId == null) {
            return Optional.empty();
        }

        return findById(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return emailIndex.containsKey(email);
    }

    @Override
    public boolean deleteById(Long id) {
        User removedUser = users.remove(id);

        if (removedUser == null) {
            return false;
        }

        emailIndex.remove(removedUser.getEmail());
        return true;
    }

}