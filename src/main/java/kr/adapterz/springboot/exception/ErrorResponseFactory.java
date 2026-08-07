package kr.adapterz.springboot.exception;

import jakarta.servlet.http.HttpServletRequest;
import kr.adapterz.springboot.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ErrorResponseFactory {

    public ResponseEntity<ApiResponseDto<?>> error(
            HttpServletRequest request,
            HttpStatus status,
            String message
    ) {
        Object data = isMyPageRequest(request) ? errorData(message) : null;
        return ResponseEntity
                .status(status)
                .body(new ApiResponseDto<>(message, data));
    }

    public ResponseEntity<ApiResponseDto<?>> validation(
            HttpServletRequest request,
            Map<String, String> errors
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDto<>("validation_failed", errors));
    }

    public ResponseEntity<ApiResponseDto<?>> fieldError(
            HttpServletRequest request,
            HttpStatus status,
            String message,
            String field
    ) {
        if (!isMyPageRequest(request)) {
            return ResponseEntity
                    .status(status)
                    .body(new ApiResponseDto<>(message, null));
        }

        Map<String, String> data = new LinkedHashMap<>();
        data.put(field, message);
        return ResponseEntity
                .status(status)
                .body(new ApiResponseDto<>(message, data));
    }

    public boolean isMyPageRequest(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return requestUri.equals("/users/me/mypage")
                || requestUri.startsWith("/users/me/mypage/");
    }

    private Map<String, String> errorData(String message) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("error", message);
        return data;
    }
}
