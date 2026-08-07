package kr.adapterz.springboot.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import kr.adapterz.springboot.auth.ForbiddenException;
import kr.adapterz.springboot.auth.UnauthorizedException;
import kr.adapterz.springboot.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final HttpServletRequest request;
    private final ErrorResponseFactory errorResponseFactory = new ErrorResponseFactory();

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseDto<?>> handleUnauthorized(UnauthorizedException e) {
        return error(HttpStatus.UNAUTHORIZED, "authentication_required");
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponseDto<?>> handleJwtException(JwtException e) {
        return error(HttpStatus.UNAUTHORIZED, "invalid_token");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponseDto<?>> handleForbidden(ForbiddenException e) {
        return error(HttpStatus.FORBIDDEN, "access_denied");
    }

    @ExceptionHandler(DeletedUserException.class)
    public ResponseEntity<ApiResponseDto<?>> handleDeletedUser(DeletedUserException e) {
        return error(HttpStatus.FORBIDDEN, "user_deleted");
    }

    @ExceptionHandler(DeletedCommentException.class)
    public ResponseEntity<ApiResponseDto<?>> handleDeletedComment(DeletedCommentException e) {
        return error(HttpStatus.CONFLICT, "comment_deleted");
    }

    @ExceptionHandler(PostRateLimitExceededException.class)
    public ResponseEntity<ApiResponseDto<?>> handlePostRateLimit(PostRateLimitExceededException e) {
        return error(HttpStatus.TOO_MANY_REQUESTS, "post_rate_limited");
    }

    @ExceptionHandler(DuplicatePostReportException.class)
    public ResponseEntity<ApiResponseDto<?>> handleDuplicatePostReport(DuplicatePostReportException e) {
        return error(HttpStatus.CONFLICT, "post_already_reported");
    }

    @ExceptionHandler(PostBlindedException.class)
    public ResponseEntity<ApiResponseDto<?>> handlePostBlinded(PostBlindedException e) {
        return error(HttpStatus.FORBIDDEN, "post_blinded");
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponseDto<?>> handlePostNotFound(PostNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "post_not_found");
    }

    @ExceptionHandler(PollNotFoundException.class)
    public ResponseEntity<ApiResponseDto<?>> handlePollNotFound(PollNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "poll_not_found");
    }

    @ExceptionHandler(PollOptionMismatchException.class)
    public ResponseEntity<ApiResponseDto<?>> handlePollOptionMismatch(PollOptionMismatchException e) {
        return error(HttpStatus.BAD_REQUEST, "poll_option_mismatch");
    }

    @ExceptionHandler(PollOptionDuplicateException.class)
    public ResponseEntity<ApiResponseDto<?>> handlePollOptionDuplicate(PollOptionDuplicateException e) {
        return error(HttpStatus.BAD_REQUEST, "poll_options_duplicate");
    }

    @ExceptionHandler(PollOptionsLockedException.class)
    public ResponseEntity<ApiResponseDto<?>> handlePollOptionsLocked(PollOptionsLockedException e) {
        return error(HttpStatus.CONFLICT, "poll_options_locked");
    }

    @ExceptionHandler(PollOptionUpdateInvalidException.class)
    public ResponseEntity<ApiResponseDto<?>> handlePollOptionUpdateInvalid(PollOptionUpdateInvalidException e) {
        return error(HttpStatus.BAD_REQUEST, "poll_options_invalid");
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponseDto<?>> handleCommentNotFound(CommentNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "comment_not_found");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDto<?>> handleUserNotFound(UserNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "user_not_found");
    }

    @ExceptionHandler(PostDraftNotFoundException.class)
    public ResponseEntity<ApiResponseDto<?>> handlePostDraftNotFound(PostDraftNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "post_draft_not_found");
    }

    @ExceptionHandler(DuplicatePostLikeException.class)
    public ResponseEntity<ApiResponseDto<?>> handleDuplicatePostLike(DuplicatePostLikeException e) {
        return error(HttpStatus.CONFLICT, "post_already_liked");
    }

    @ExceptionHandler(PostLikeNotFoundException.class)
    public ResponseEntity<ApiResponseDto<?>> handlePostLikeNotFound(PostLikeNotFoundException e) {
        return error(HttpStatus.CONFLICT, "post_not_liked");
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponseDto<?>> handleDuplicateEmail(DuplicateEmailException e) {
        return error(HttpStatus.CONFLICT, "email_already_exists");
    }

    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<ApiResponseDto<?>> handleDuplicateNickname(DuplicateNicknameException e) {
        return error(HttpStatus.CONFLICT, "nickname_already_exists");
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ApiResponseDto<?>> handleInvalidLogin(InvalidLoginException e) {
        return error(HttpStatus.UNAUTHORIZED, "invalid_login");
    }

    @ExceptionHandler(ParentCommentMismatchException.class)
    public ResponseEntity<ApiResponseDto<?>> handleParentCommentMismatch(ParentCommentMismatchException e) {
        return error(HttpStatus.BAD_REQUEST, "parent_comment_mismatch");
    }

    @ExceptionHandler(InvalidImageFileException.class)
    public ResponseEntity<ApiResponseDto<?>> handleInvalidImageFile(InvalidImageFileException e) {
        return error(HttpStatus.BAD_REQUEST, "invalid_image_file");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDto<?>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, "image_file_too_large");
    }

    @ExceptionHandler(ImageUploadFailedException.class)
    public ResponseEntity<ApiResponseDto<?>> handleImageUploadFailed(ImageUploadFailedException e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "image_upload_failed");
    }

    @ExceptionHandler(InvalidCursorException.class)
    public ResponseEntity<ApiResponseDto<?>> handleInvalidCursor(InvalidCursorException e) {
        return errorResponseFactory.fieldError(
                request,
                HttpStatus.BAD_REQUEST,
                "invalid_cursor",
                "cursor"
        );
    }

    @ExceptionHandler(MyPageReadFailedException.class)
    public ResponseEntity<ApiResponseDto<?>> handleMyPageReadFailed(MyPageReadFailedException e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "mypage_read_failed");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<?>> handleValidation(MethodArgumentNotValidException e) {
        return validationError(e);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponseDto<?>> handleBindException(BindException e) {
        return validationError(e);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<?>> handleUnreadableRequest(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "invalid_request_body");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<?>> handleException(Exception e) {
        String message = errorResponseFactory.isMyPageRequest(request)
                ? "mypage_read_failed"
                : "internal_server_error";
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private ResponseEntity<ApiResponseDto<?>> error(HttpStatus status, String message) {
        return errorResponseFactory.error(request, status, message);
    }

    private ResponseEntity<ApiResponseDto<?>> validationError(BindException e) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return errorResponseFactory.validation(request, errors);
    }
}
