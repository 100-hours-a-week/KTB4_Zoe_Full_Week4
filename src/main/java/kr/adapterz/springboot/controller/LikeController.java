package kr.adapterz.springboot.controller;

import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.LikeResponseDto;
import kr.adapterz.springboot.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/posts/{post_id}")
    public ResponseEntity<ApiResponseDto<LikeResponseDto>> likePost(
            @PathVariable("post_id") Long postId
    ) {
        LikeResponseDto response = likeService.likePost(postId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("post_like_created", response));
    }

    @DeleteMapping("/posts/{post_id}")
    public ResponseEntity<ApiResponseDto<LikeResponseDto>> unlikePost(
            @PathVariable("post_id") Long postId
    ) {
        LikeResponseDto response = likeService.unlikePost(postId);

        return ResponseEntity.ok(new ApiResponseDto<>("post_like_deleted", response));
    }
}
