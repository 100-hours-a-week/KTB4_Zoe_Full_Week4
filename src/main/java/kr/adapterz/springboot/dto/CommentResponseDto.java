package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {

    private static final String DELETED_USER_NICKNAME = "알 수 없음";

    @JsonProperty("comment_id")
    private Long commentId;

    @JsonProperty("user_id")
    private Long userId;

    private String nickname;

    @JsonProperty("profile_image")
    private String profileImage;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    private String content;

    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getId();
        this.userId = comment.getAuthor().getId();

        if (comment.getAuthor().isDeleted()) {
            this.nickname = DELETED_USER_NICKNAME;
            this.profileImage = null;
        } else {
            this.nickname = comment.getAuthor().getNickname();
            this.profileImage = comment.getAuthor().getProfileImage();
        }

        this.createdAt = comment.getCreatedAt();
        this.content = comment.getContent();
    }
}
