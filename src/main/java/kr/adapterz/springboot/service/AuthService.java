package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.JwtTokenProvider;
import kr.adapterz.springboot.auth.TokenPair;
import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.LoginRequestDto;
import kr.adapterz.springboot.dto.PasswordChangeRequestDto;
import kr.adapterz.springboot.dto.SignupRequestDto;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.DeletedUserException;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CurrentUserProvider currentUserProvider;

    public void signup(SignupRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = User.of(
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                request.getProfileImage()
        );

        userRepository.save(user);
    }

    public TokenPair login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        validateActiveUser(user);

        return jwtTokenProvider.createTokenPair(user.getId()); //토큰 반환

    }

    public TokenPair reissue(String refreshToken) {
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        validateActiveUser(user);

        return jwtTokenProvider.createTokenPair(userId);
    }

    public void changePassword(PasswordChangeRequestDto request) {
        Long userId = currentUserProvider.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        validateActiveUser(user);

        user.changePassword(request.getPassword());

        userRepository.save(user);
    }

    private void validateActiveUser(User user) {
        if (user.isDeleted()) {
            throw new DeletedUserException();
        }
    }
}
