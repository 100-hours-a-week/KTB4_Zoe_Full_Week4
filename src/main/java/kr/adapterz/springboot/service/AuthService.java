package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.JwtTokenProvider;
import kr.adapterz.springboot.auth.LoginResult;
import kr.adapterz.springboot.auth.TokenPair;
import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.LoginRequestDto;
import kr.adapterz.springboot.dto.MultipartSignupRequestDto;
import kr.adapterz.springboot.dto.PasswordChangeRequestDto;
import kr.adapterz.springboot.dto.SignupRequestDto;
import kr.adapterz.springboot.dto.UserResponseDto;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.DeletedUserException;
import kr.adapterz.springboot.exception.DuplicateEmailException;
import kr.adapterz.springboot.exception.DuplicateNicknameException;
import kr.adapterz.springboot.exception.InvalidLoginException;
import kr.adapterz.springboot.exception.UserNotFoundException;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CurrentUserProvider currentUserProvider;
    private final ImageStorageService imageStorageService;

    public void signup(SignupRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateNicknameException();
        }

        User user = User.of(
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                request.getProfileImage()
        );

        userRepository.save(user);
    }

    public void signup(MultipartSignupRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateNicknameException();
        }

        String profileImageUrl = imageStorageService.storeProfileImage(request.getProfileImage());
        User user = User.of(
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                profileImageUrl
        );

        userRepository.save(user);
    }

    public LoginResult login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidLoginException::new);

        if (!user.getPassword().equals(request.getPassword())) {
            throw new InvalidLoginException();
        }

        validateActiveUser(user);

        TokenPair tokens = jwtTokenProvider.createTokenPair(user.getId());
        return new LoginResult(tokens, new UserResponseDto(user));
    }

    public TokenPair reissue(String refreshToken) {
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        validateActiveUser(user);

        return jwtTokenProvider.createTokenPair(userId);
    }

    public void changePassword(PasswordChangeRequestDto request) {
        Long userId = currentUserProvider.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

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
