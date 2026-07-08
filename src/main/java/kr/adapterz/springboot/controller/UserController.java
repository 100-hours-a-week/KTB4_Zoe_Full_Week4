package kr.adapterz.springboot.controller;

import jakarta.validation.Valid;
import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.MultipartUserUpdateRequestDto;
import kr.adapterz.springboot.dto.UserResponseDto;
import kr.adapterz.springboot.dto.UserUpdateResponseDto;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getUser() {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userService.getUser(userId);
        UserResponseDto response = new UserResponseDto(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponseDto<>("fetch_success", response));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<UserUpdateResponseDto>> putUserWithImage(
            @Valid @ModelAttribute MultipartUserUpdateRequestDto request
    ) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userService.updateUser(userId, request);
        UserUpdateResponseDto response = new UserUpdateResponseDto(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponseDto<>("user_updated", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponseDto<Void>> deleteUser() {
        Long userId = currentUserProvider.getCurrentUserId();
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponseDto<>("user_deleted", null));
    }

}
