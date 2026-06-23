package kr.adapterz.springboot.service;

import kr.adapterz.springboot.dto.UserUpdateRequestDto;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.DeletedUserException;
import kr.adapterz.springboot.exception.UserNotFoundException;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        validateActiveUser(user);
        return user;
    }

    @Transactional
    public User updateUser(Long userId, UserUpdateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        validateActiveUser(user);

        user.changeNickname(request.getNickname());
        user.changeProfileImage(request.getProfileImage());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        validateActiveUser(user);

        user.delete();
        userRepository.save(user);
    }

    private void validateActiveUser(User user) {
        if (user.isDeleted()) {
            throw new DeletedUserException();
        }
    }
}
