package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostDetailResponseDto {

    @JsonProperty("post_id")
    private Long postId;

    private String title;
    private WriterResponseDto writer;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("image_url")
    private String imageUrl;

    private String content;

    @JsonProperty("like_count")
    private long likeCount;

    @JsonProperty("comment_count")
    private long commentCount;

    @JsonProperty("view_count")
    private long viewCount;

    @JsonProperty("is_liked")
    private boolean liked;

    public PostDetailResponseDto(
            Post post,
            long commentCount,
            long likeCount,
            boolean liked
    ) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.writer = new WriterResponseDto(post.getAuthor());
        this.createdAt = post.getCreatedAt();
        this.imageUrl = post.getImageUrl();
        this.content = post.getContent();
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.viewCount = post.getViewCount();
        this.liked = liked;
    }
}
