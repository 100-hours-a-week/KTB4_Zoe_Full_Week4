package kr.adapterz.springboot.controller;

import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.PostCreateResponseDto;
import kr.adapterz.springboot.dto.PostDetailResponseDto;
import kr.adapterz.springboot.dto.PostListResponseDto;
import kr.adapterz.springboot.dto.PostRequestDto;
import kr.adapterz.springboot.dto.PostUpdateResponseDto;
import kr.adapterz.springboot.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<PostCreateResponseDto>> createPost(@RequestBody PostRequestDto request) {
        PostCreateResponseDto response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("post_created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PostListResponseDto>> getPosts() {
        return ResponseEntity.ok(new ApiResponseDto<>("fetch_success", postService.getPosts()));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponseDto<PostDetailResponseDto>> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(new ApiResponseDto<>("fetch_success", postService.getPost(postId)));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponseDto<PostUpdateResponseDto>> updatePost(
            @PathVariable Long postId,
            @RequestBody PostRequestDto request
    ) {
        PostUpdateResponseDto response = postService.updatePost(postId, request);
        return ResponseEntity.ok(new ApiResponseDto<>("post_updated", response));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponseDto<Void>> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok(new ApiResponseDto<>("post_deleted", null));
    }

}
