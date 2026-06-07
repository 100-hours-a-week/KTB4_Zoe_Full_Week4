package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostSummaryResponseDto {

    @JsonProperty("post_id")
    private Long postId;

    private String title;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("like_count")
    private long likeCount;

    @JsonProperty("comment_count")
    private long commentCount;

    @JsonProperty("view_count")
    private long viewCount;

    private WriterResponseDto writer;

    public PostSummaryResponseDto(Post post, long likeCount, long commentCount) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.createdAt = post.getCreatedAt();
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = post.getViewCount();
        this.writer = new WriterResponseDto(post.getAuthor());
    }
}
