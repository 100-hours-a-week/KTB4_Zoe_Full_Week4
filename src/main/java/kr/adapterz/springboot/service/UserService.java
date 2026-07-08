package kr.adapterz.springboot.service;

import kr.adapterz.springboot.dto.MultipartUserUpdateRequestDto;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.DeletedUserException;
import kr.adapterz.springboot.exception.DuplicateNicknameException;
import kr.adapterz.springboot.exception.UserNotFoundException;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        validateActiveUser(user);
        return user;
    }

    @Transactional
    public User updateUser(Long userId, MultipartUserUpdateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        validateActiveUser(user);
        validateNicknameNotDuplicated(request.getNickname(), user.getId());

        String profileImageUrl = imageStorageService.storeProfileImage(request.getProfileImage());

        user.changeNickname(request.getNickname());
        if (profileImageUrl != null) {
            user.changeProfileImage(profileImageUrl);
        }

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

    private void validateNicknameNotDuplicated(String nickname, Long userId) {
        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new DuplicateNicknameException();
        }
    }
}
