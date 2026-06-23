package kr.adapterz.springboot.exception;

import kr.adapterz.springboot.auth.ForbiddenException;
import kr.adapterz.springboot.auth.UnauthorizedException;
import kr.adapterz.springboot.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUnauthorized(UnauthorizedException e) {
        return error(HttpStatus.UNAUTHORIZED, "authentication_required");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleForbidden(ForbiddenException e) {
        return error(HttpStatus.FORBIDDEN, "access_denied");
    }

    @ExceptionHandler(DeletedUserException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDeletedUser(DeletedUserException e) {
        return error(HttpStatus.FORBIDDEN, "user_deleted");
    }

    @ExceptionHandler(PostRateLimitExceededException.class)
    public ResponseEntity<ApiResponseDto<Void>> handlePostRateLimit(PostRateLimitExceededException e) {
        return error(HttpStatus.TOO_MANY_REQUESTS, "post_rate_limited");
    }

    @ExceptionHandler(DuplicatePostReportException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDuplicatePostReport(DuplicatePostReportException e) {
        return error(HttpStatus.CONFLICT, "post_already_reported");
    }

    @ExceptionHandler(PostBlindedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handlePostBlinded(PostBlindedException e) {
        return error(HttpStatus.FORBIDDEN, "post_blinded");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDto<>("validation_failed", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgument(IllegalArgumentException e) {
        String message = e.getMessage();

        if (message != null && message.contains("게시글을 찾을 수 없습니다.")) {
            return error(HttpStatus.NOT_FOUND, "post_not_found");
        }

        if (message != null && message.contains("댓글을 찾을 수 없습니다.")) {
            return error(HttpStatus.NOT_FOUND, "comment_not_found");
        }

        if (message != null && (message.contains("유저를 찾을 수 없습니다.") || message.contains("사용자를 찾을 수 없습니다."))) {
            return error(HttpStatus.NOT_FOUND, "user_not_found");
        }

        if (message != null && message.contains("이미 좋아요를 누른 게시글입니다.")) {
            return error(HttpStatus.CONFLICT, "post_already_liked");
        }

        if (message != null && message.contains("좋아요를 누르지 않은 게시글입니다.")) {
            return error(HttpStatus.CONFLICT, "post_not_liked");
        }

        return error(HttpStatus.BAD_REQUEST, "invalid_request");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleException(Exception e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error");
    }

    private ResponseEntity<ApiResponseDto<Void>> error(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponseDto<>(message, null));
    }
}
