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

    //스프링 빈이 생성되고 의존성 주입이 끝난 뒤 초기화 로직을 실행할 때 사용하는 어노테이션
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    //토큰 생성
    private String createToken(String type, Long userId, Map<String, Object> claims, long expSeconds) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("typ", type)
                .claims(claims) //jwt 안에 담기는 사용자 정보와 토큰 속성 데이터
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith((SecretKey) key, Jwts.SIG.HS256) // 비밀키로 형변환
                .compact();
    }

    //토큰쌍 생성
    public TokenPair createTokenPair(Long userId, String email, String nickname) {
        String accessToken = createToken("access", userId, Map.of("email", email, "nickname", nickname), jwtProperties.getAccessTokenExpSeconds());
        String refreshToken = createToken("refresh", userId, Map.of(), jwtProperties.getRefreshTokenExpSeconds());

        return new TokenPair(accessToken, refreshToken);
    }

    //토큰 파싱
    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token);
    }

    // 엑세스토큰 여부 확인 메서드
    public boolean isAccessToken(String token) {
        return "access".equals(parse(token).getPayload().get("typ", String.class));
    }

    // 토큰에서 유저아이디 꺼내오기
    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getPayload().getSubject());
    }

    // 토큰 유효기간 단위 밀리초로 바꾸는 메서드
    public Long getAccessTokenValidityInMilliseconds() {
        return jwtProperties.getAccessTokenExpSeconds() * 1000;
    }

}
