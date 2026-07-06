package kr.adapterz.springboot.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    private String createToken(String type, Long userId, Map<String, Object> claims, long expSeconds) {
        Instant now = Instant.now();

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .claim("typ", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith((SecretKey) key, Jwts.SIG.HS256)
                .compact();
    }

    public TokenPair createTokenPair(Long userId, String email, String nickname) {
        String accessToken = createToken("access", userId, Map.of("email", email, "nickname", nickname), jwtProperties.getAccessTokenExpSeconds());
        String refreshToken = createToken("refresh", userId, Map.of(), jwtProperties.getRefreshTokenExpSeconds());

        return new TokenPair(accessToken, refreshToken);
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token);
    }


    public Claims getAccessTokenClaims(String token) {
        Claims claims = parse(token).getPayload();

        if (!"access".equals(claims.get("typ", String.class))) {
            throw new JwtException("Not an access token");
        }

        return claims;
    }

    public Claims getRefreshTokenClaims(String token) {
        Claims claims = parse(token).getPayload();

        if (!"refresh".equals(claims.get("typ", String.class))) {
            throw new JwtException("Not a refresh token");
        }

        return claims;
    }

}
