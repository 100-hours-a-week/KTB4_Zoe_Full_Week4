package kr.adapterz.springboot.service;

import io.jsonwebtoken.Claims;
import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.auth.JwtTokenProvider;
import kr.adapterz.springboot.auth.LoginResult;
import kr.adapterz.springboot.auth.TokenPair;
import kr.adapterz.springboot.auth.UnauthorizedException;
import kr.adapterz.springboot.dto.LoginRequestDto;
import kr.adapterz.springboot.dto.MultipartSignupRequestDto;
import kr.adapterz.springboot.dto.PasswordChangeRequestDto;
import kr.adapterz.springboot.dto.UserResponseDto;
import kr.adapterz.springboot.entity.RefreshToken;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.DeletedUserException;
import kr.adapterz.springboot.exception.DuplicateEmailException;
import kr.adapterz.springboot.exception.DuplicateNicknameException;
import kr.adapterz.springboot.exception.InvalidLoginException;
import kr.adapterz.springboot.exception.UserNotFoundException;
import kr.adapterz.springboot.repository.RefreshTokenRepository;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CurrentUserProvider currentUserProvider;
    private final ImageStorageService imageStorageService;
    private final PasswordEncoder passwordEncoder;

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
                passwordEncoder.encode(request.getPassword()),
                request.getNickname(),
                profileImageUrl
        );

        userRepository.save(user);
    }

    public LoginResult login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidLoginException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidLoginException();
        }

        validateActiveUser(user);

        TokenPair tokens = jwtTokenProvider.createTokenPair(user.getId(), user.getEmail(), user.getNickname());
        saveRefreshToken(user.getId(), tokens.refreshToken());

        return new LoginResult(tokens, new UserResponseDto(user));
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    public TokenPair reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException();
        }

        Claims claims = jwtTokenProvider.getRefreshTokenClaims(refreshToken);
        Long userId = Long.valueOf(claims.getSubject());

        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(UnauthorizedException::new);

        if (savedToken.isExpired() || !savedToken.hasToken(refreshToken)) {
            refreshTokenRepository.delete(savedToken);
            throw new UnauthorizedException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        validateActiveUser(user);

        TokenPair tokens = jwtTokenProvider.createTokenPair(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );

        savedToken.rotate(tokens.refreshToken(), getExpiresAt(tokens.refreshToken()));
        refreshTokenRepository.save(savedToken);

        return tokens;
    }

    public void changePassword(PasswordChangeRequestDto request) {
        Long userId = currentUserProvider.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        validateActiveUser(user);

        user.changePassword(passwordEncoder.encode(request.getPassword()));
    }

    private void validateActiveUser(User user) {
        if (user.isDeleted()) {
            throw new DeletedUserException();
        }
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        LocalDateTime expiresAt = getExpiresAt(refreshToken);

        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        savedToken -> {
                            savedToken.rotate(refreshToken, expiresAt);
                            refreshTokenRepository.save(savedToken);
                        },
                        () -> refreshTokenRepository.save(new RefreshToken(refreshToken, userId, expiresAt))
                );
    }

    private LocalDateTime getExpiresAt(String token) {
        Claims claims = jwtTokenProvider.parse(token).getPayload();
        return LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
    }
}
