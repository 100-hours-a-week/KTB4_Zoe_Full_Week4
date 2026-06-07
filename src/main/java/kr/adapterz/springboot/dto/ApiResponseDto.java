package kr.adapterz.springboot.dto;

import lombok.Getter;

@Getter
public class ApiResponseDto<T> {

    private String message;
    private T data;

    public ApiResponseDto(String message, T data) {
        this.message = message;
        this.data = data;
    }
}
