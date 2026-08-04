package kr.adapterz.springboot.exception;

import io.jsonwebtoken.JwtException;
import kr.adapterz.springboot.auth.ForbiddenException;
import kr.adapterz.springboot.auth.UnauthorizedException;
import kr.adapterz.springboot.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
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

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleJwtException(JwtException e) {
        return error(HttpStatus.UNAUTHORIZED, "invalid_token");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleForbidden(ForbiddenException e) {
        return error(HttpStatus.FORBIDDEN, "access_denied");
    }

    @ExceptionHandler(DeletedUserException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDeletedUser(DeletedUserException e) {
        return error(HttpStatus.FORBIDDEN, "user_deleted");
    }

    @ExceptionHandler(DeletedCommentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDeletedComment(DeletedCommentException e) {
        return error(HttpStatus.CONFLICT, "comment_deleted");
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

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handlePostNotFound(PostNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "post_not_found");
    }

    @ExceptionHandler(PollNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handlePollNotFound(PollNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "poll_not_found");
    }

    @ExceptionHandler(PollOptionMismatchException.class)
    public ResponseEntity<ApiResponseDto<Void>> handlePollOptionMismatch(PollOptionMismatchException e) {
        return error(HttpStatus.BAD_REQUEST, "poll_option_mismatch");
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleCommentNotFound(CommentNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "comment_not_found");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserNotFound(UserNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "user_not_found");
    }

    @ExceptionHandler(PostDraftNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handlePostDraftNotFound(PostDraftNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "post_draft_not_found");
    }

    @ExceptionHandler(DuplicatePostLikeException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDuplicatePostLike(DuplicatePostLikeException e) {
        return error(HttpStatus.CONFLICT, "post_already_liked");
    }

    @ExceptionHandler(PostLikeNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handlePostLikeNotFound(PostLikeNotFoundException e) {
        return error(HttpStatus.CONFLICT, "post_not_liked");
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDuplicateEmail(DuplicateEmailException e) {
        return error(HttpStatus.CONFLICT, "email_already_exists");
    }

    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDuplicateNickname(DuplicateNicknameException e) {
        return error(HttpStatus.CONFLICT, "nickname_already_exists");
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidLogin(InvalidLoginException e) {
        return error(HttpStatus.UNAUTHORIZED, "invalid_login");
    }

    @ExceptionHandler(ParentCommentMismatchException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleParentCommentMismatch(ParentCommentMismatchException e) {
        return error(HttpStatus.BAD_REQUEST, "parent_comment_mismatch");
    }

    @ExceptionHandler(InvalidImageFileException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidImageFile(InvalidImageFileException e) {
        return error(HttpStatus.BAD_REQUEST, "invalid_image_file");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, "image_file_too_large");
    }

    @ExceptionHandler(ImageUploadFailedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleImageUploadFailed(ImageUploadFailedException e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "image_upload_failed");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> handleValidation(MethodArgumentNotValidException e) {
        return validationError(e);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> handleBindException(BindException e) {
        return validationError(e);
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

    private ResponseEntity<ApiResponseDto<Map<String, String>>> validationError(BindException e) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDto<>("validation_failed", errors));
    }
}
