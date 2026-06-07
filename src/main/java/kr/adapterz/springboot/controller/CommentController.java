package kr.adapterz.springboot.controller;

import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.CommentListResponseDto;
import kr.adapterz.springboot.dto.CommentRequestDto;
import kr.adapterz.springboot.dto.CommentResponseDto;
import kr.adapterz.springboot.entity.Comment;
import kr.adapterz.springboot.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{post_id}")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> createComment(@PathVariable("post_id") Long postId, @RequestBody CommentRequestDto request) {

        Comment comment = commentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("comment_created", new CommentResponseDto(comment)));
    }

    @GetMapping("/posts/{post_id}")
    public ResponseEntity<ApiResponseDto<CommentListResponseDto>> getComments(
            @PathVariable("post_id") Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        CommentListResponseDto response = commentService.getComments(postId, page, size);

        return ResponseEntity.ok(new ApiResponseDto<>("fetch_success", response));
    }

    @PutMapping("/{comment_id}")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> updateComment(
            @PathVariable("comment_id") Long commentId,
            @RequestBody CommentRequestDto request
    ) {
        Comment comment = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(new ApiResponseDto<>("comment_updated", new CommentResponseDto(comment)));
    }

    @DeleteMapping("/{comment_id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteComment(
            @PathVariable("comment_id") Long commentId
    ) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new ApiResponseDto<>("comment_deleted", null));
    }
}
