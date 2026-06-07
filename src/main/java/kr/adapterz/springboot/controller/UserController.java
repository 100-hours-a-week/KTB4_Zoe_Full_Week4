package kr.adapterz.springboot.controller;

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
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long userId) {
        User user = userService.getUser(userId);
        UserResponseDto response = new UserResponseDto(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> putUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequestDto request
    ) {
        User user = userService.updateUser(userId, request);
        UserResponseDto response = new UserResponseDto(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

}
