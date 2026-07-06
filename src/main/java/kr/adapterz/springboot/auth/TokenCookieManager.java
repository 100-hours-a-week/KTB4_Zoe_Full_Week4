package kr.adapterz.springboot.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenCookieManager {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/";
    private static final String SAME_SITE = "Lax";

    private final JwtProperties jwtProperties;

    public void addTokenCookies(HttpServletResponse response, TokenPair tokens) {
        addCookie(response, createCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                tokens.accessToken(),
                jwtProperties.getAccessTokenExpSeconds()
        ));
        addCookie(response, createCookie(
                REFRESH_TOKEN_COOKIE_NAME,
                tokens.refreshToken(),
                jwtProperties.getRefreshTokenExpSeconds()
        ));
    }

    public void expireTokenCookies(HttpServletResponse response) {
        addCookie(response, createCookie(ACCESS_TOKEN_COOKIE_NAME, "", 0));
        addCookie(response, createCookie(REFRESH_TOKEN_COOKIE_NAME, "", 0));
    }

    private ResponseCookie createCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .sameSite(SAME_SITE)
                .path(COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .build();
    }

    private void addCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
