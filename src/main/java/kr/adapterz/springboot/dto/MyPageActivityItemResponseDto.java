package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyPageActivityItemResponseDto {

    @JsonProperty("post_id")
    private final Long postId;

    private final String title;

    @JsonProperty("activity_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime activityAt;

    @JsonProperty("post_created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime postCreatedAt;

    @JsonProperty("like_count")
    private final long likeCount;

    @JsonProperty("comment_count")
    private final long commentCount;

    @JsonProperty("participant_count")
    private final long participantCount;

    @JsonProperty("is_voted")
    private final boolean voted;

    public MyPageActivityItemResponseDto(
            Long postId,
            String title,
            LocalDateTime activityAt,
            LocalDateTime postCreatedAt,
            long likeCount,
            long commentCount,
            long participantCount,
            boolean voted
    ) {
        this.postId = postId;
        this.title = title;
        this.activityAt = activityAt;
        this.postCreatedAt = postCreatedAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.participantCount = participantCount;
        this.voted = voted;
    }
}
