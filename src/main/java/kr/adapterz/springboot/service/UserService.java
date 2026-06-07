package kr.adapterz.springboot.service;

import kr.adapterz.springboot.dto.UserUpdateRequestDto;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    }

    public User updateUser(Long userId, UserUpdateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        user.changeNickname(request.getNickname());
        user.changeProfileImage(request.getProfileImage());

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        boolean deleted = userRepository.deleteById(userId);

        if (!deleted) {
            throw new IllegalArgumentException("삭제할 사용자를 찾을 수 없습니다.");
        }
    }
}
