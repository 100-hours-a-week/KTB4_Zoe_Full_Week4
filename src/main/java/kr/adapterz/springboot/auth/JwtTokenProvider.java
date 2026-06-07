package kr.adapterz.springboot.auth;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key = Keys.hmacShaKeyFor(
            "my-super-secret-key-for-jwt-32chars".getBytes(StandardCharsets.UTF_8)
    );

    //토큰 생성
    public TokenPair createTokenPair(Long userId) {
        String accessToken = createToken(userId, 1000 * 60 * 60); // 1시간
        String refreshToken = createToken(userId, 1000L * 60 * 60 * 24 * 14); // 14일

        return new TokenPair(accessToken, refreshToken);
    }

    private String createToken(Long userId, long expirationMillis) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }


    //토큰 검증
    public Long getUserId(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            return Long.parseLong(subject);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException();
        }
    }
}
