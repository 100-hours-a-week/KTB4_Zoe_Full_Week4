package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostDetailResponseDto {

    @JsonProperty("post_id")
    private Long postId;

    private String title;
    private WriterResponseDto writer;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("image_urls")
    private List<String> imageUrls;

    private String content;

    @JsonProperty("like_count")
    private long likeCount;

    @JsonProperty("comment_count")
    private long commentCount;

    @JsonProperty("view_count")
    private long viewCount;

    @JsonProperty("is_liked")
    private boolean liked;

    @JsonProperty("is_edited")
    private boolean edited;

    private PollResponseDto poll;

    public PostDetailResponseDto(
            Post post,
            long commentCount,
            long likeCount,
            boolean liked,
            boolean edited,
            PollResponseDto poll
    ) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.writer = new WriterResponseDto(post.getAuthor());
        this.createdAt = post.getCreatedAt();
        this.imageUrls = post.getImages().stream()
                .map(image -> image.getImageUrl())
                .toList();
        this.content = post.getContent();
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.viewCount = post.getViewCount();
        this.liked = liked;
        this.edited = edited;
        this.poll = poll;
    }
}
