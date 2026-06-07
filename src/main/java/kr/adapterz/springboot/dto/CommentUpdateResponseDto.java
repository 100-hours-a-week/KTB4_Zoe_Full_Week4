package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Comment;
import lombok.Getter;

@Getter
public class CommentUpdateResponseDto {

    @JsonProperty("comment_id")
    private Long commentId;

    private String content;

    public CommentUpdateResponseDto(Comment comment) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
    }
}
