package kr.adapterz.springboot.controller;

import kr.adapterz.springboot.dto.CommentRequestDto;
import kr.adapterz.springboot.dto.CommentResponseDto;
import kr.adapterz.springboot.entity.Comment;
import kr.adapterz.springboot.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{post_id}")
    public ResponseEntity<CommentResponseDto> createComment(@PathVariable("post_id") Long postId, @RequestBody CommentRequestDto request) {

        Comment comment = commentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommentResponseDto(comment));
    }

    @GetMapping("/posts/{post_id}")
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @PathVariable("post_id") Long postId
    ) {
        List<CommentResponseDto> response = commentService.getComments(postId).stream()
                .map(CommentResponseDto::new)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{comment_id}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable("comment_id") Long commentId,
            @RequestBody CommentRequestDto request
    ) {
        Comment comment = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(new CommentResponseDto(comment));
    }

    @DeleteMapping("/{comment_id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("comment_id") Long commentId
    ) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
