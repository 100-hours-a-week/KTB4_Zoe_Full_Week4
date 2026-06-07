package kr.adapterz.springboot.auth;

public record TokenPair(String accessToken, String refreshToken) {
}
