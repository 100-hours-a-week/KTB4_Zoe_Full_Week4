package kr.adapterz.springboot.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import kr.adapterz.springboot.auth.CurrentUserProvider;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class JwtCurrentUserProvider implements CurrentUserProvider {

    private final HttpServletRequest request;
    private final JwtTokenProvider jwtTokenProvider;

    public JwtCurrentUserProvider(
            HttpServletRequest request,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.request = request;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Long getCurrentUserId() {
        String token = resolveToken(request);
        return jwtTokenProvider.getUserId(token);
    }

    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new UnauthorizedException();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(UnauthorizedException::new);
    }
}