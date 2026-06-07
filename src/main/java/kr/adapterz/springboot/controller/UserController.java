package kr.adapterz.springboot.controller;

import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.UserUpdateRequestDto;
import kr.adapterz.springboot.dto.UserResponseDto;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getUser(@PathVariable Long userId) {
        User user = userService.getUser(userId);
        UserResponseDto response = new UserResponseDto(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponseDto<>("fetch_success", response));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> putUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequestDto request
    ) {
        User user = userService.updateUser(userId, request);
        UserResponseDto response = new UserResponseDto(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponseDto<>("user_updated", response));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponseDto<>("user_deleted", null));
    }

}
