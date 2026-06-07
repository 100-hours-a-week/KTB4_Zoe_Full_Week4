package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Comment;
import lombok.Getter;

@Getter
public class CommentCreateResponseDto {

    @JsonProperty("comment_id")
    private Long commentId;

    public CommentCreateResponseDto(Comment comment) {
        this.commentId = comment.getId();
    }
}
