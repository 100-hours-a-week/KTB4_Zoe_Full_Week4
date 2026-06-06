package kr.adapterz.springboot.auth;

import jakarta.servlet.http.HttpServletRequest;
import kr.adapterz.springboot.auth.CurrentUserProvider;
import org.springframework.stereotype.Component;

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
        String bearer = request.getHeader("Authorization");

        if (bearer == null || !bearer.startsWith("Bearer ")) {
            throw new UnauthorizedException();
        }

        return bearer.substring(7);
    }
}