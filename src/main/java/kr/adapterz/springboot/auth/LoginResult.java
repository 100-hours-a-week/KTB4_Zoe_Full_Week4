package kr.adapterz.springboot.auth;

import kr.adapterz.springboot.dto.UserResponseDto;

public record LoginResult(TokenPair tokens, UserResponseDto user) {
}
