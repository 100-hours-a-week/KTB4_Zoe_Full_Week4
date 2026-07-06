package kr.adapterz.springboot.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.adapterz.springboot.auth.LoginResult;
import kr.adapterz.springboot.auth.TokenPair;
import kr.adapterz.springboot.auth.TokenCookieManager;
import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.LoginRequestDto;
import kr.adapterz.springboot.dto.MultipartSignupRequestDto;
import kr.adapterz.springboot.dto.PasswordChangeRequestDto;
import kr.adapterz.springboot.dto.SignupRequestDto;
import kr.adapterz.springboot.dto.UserResponseDto;
import kr.adapterz.springboot.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenCookieManager tokenCookieManager;

    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Void>> signup(@Valid @RequestBody SignupRequestDto request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("user_created", null));
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<Void>> signupWithImage(@Valid @ModelAttribute MultipartSignupRequestDto request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("user_created", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response
    ) {
        LoginResult loginResult = authService.login(request);
        TokenPair tokens = loginResult.tokens();
        tokenCookieManager.addTokenCookies(response, tokens);

        return ResponseEntity.ok(new ApiResponseDto<>("login_success", loginResult.user()));
    }

    //로그아웃시 쿠키 삭제
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        tokenCookieManager.expireTokenCookies(response);

        return ResponseEntity.ok(new ApiResponseDto<>("logout_success", null));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponseDto<Void>> reissue(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        TokenPair tokens = authService.reissue(refreshToken);
        tokenCookieManager.addTokenCookies(response, tokens);

        return ResponseEntity.ok(new ApiResponseDto<>("token_reissued", null));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponseDto<Void>> changePassword(
            @Valid @RequestBody PasswordChangeRequestDto request
    ) {
        authService.changePassword(request);
        return ResponseEntity.ok(new ApiResponseDto<>("password_updated", null));
    }
}
