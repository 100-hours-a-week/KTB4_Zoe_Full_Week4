package kr.adapterz.springboot.controller;

import jakarta.validation.Valid;
import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.MultipartPostRequestDto;
import kr.adapterz.springboot.dto.PostCreateResponseDto;
import kr.adapterz.springboot.dto.PostDetailResponseDto;
import kr.adapterz.springboot.dto.PostListResponseDto;
import kr.adapterz.springboot.dto.PostRequestDto;
import kr.adapterz.springboot.dto.PostUpdateResponseDto;
import kr.adapterz.springboot.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<PostCreateResponseDto>> createPost(@Valid @RequestBody PostRequestDto request) {
        PostCreateResponseDto response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("post_created", response));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<PostCreateResponseDto>> createPostWithImages(
            @Valid @ModelAttribute MultipartPostRequestDto request
    ) {
        PostCreateResponseDto response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("post_created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PostListResponseDto>> getPosts(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(new ApiResponseDto<>("fetch_success", postService.getPosts(cursor, size)));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponseDto<PostDetailResponseDto>> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(new ApiResponseDto<>("fetch_success", postService.getPost(postId)));
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<PostUpdateResponseDto>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequestDto request
    ) {
        PostUpdateResponseDto response = postService.updatePost(postId, request);
        return ResponseEntity.ok(new ApiResponseDto<>("post_updated", response));
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<PostUpdateResponseDto>> updatePostWithImages(
            @PathVariable Long postId,
            @Valid @ModelAttribute MultipartPostRequestDto request
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
