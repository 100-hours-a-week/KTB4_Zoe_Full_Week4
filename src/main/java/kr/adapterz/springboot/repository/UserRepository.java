package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long Id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean deleteById(Long Id);
}
