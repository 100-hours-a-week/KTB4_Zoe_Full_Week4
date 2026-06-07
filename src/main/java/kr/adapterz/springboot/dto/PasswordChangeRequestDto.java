package kr.adapterz.springboot.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PasswordChangeRequestDto {
    private String password;
}
