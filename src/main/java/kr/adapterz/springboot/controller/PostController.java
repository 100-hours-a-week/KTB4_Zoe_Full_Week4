package kr.adapterz.springboot.controller;

import kr.adapterz.springboot.dto.PostRequestDto;
import kr.adapterz.springboot.dto.PostResponseDto;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@RequestBody PostRequestDto request) {
        Post post = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PostResponseDto(post));
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getPosts() {
        List<PostResponseDto> response = postService.getPosts().stream()
                .map(PostResponseDto::new)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId) {
        Post post = postService.getPost(postId);
        return ResponseEntity.ok(new PostResponseDto(post));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @RequestBody PostRequestDto request
    ) {
        Post post = postService.updatePost(postId, request);
        return ResponseEntity.ok(new PostResponseDto(post));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

}