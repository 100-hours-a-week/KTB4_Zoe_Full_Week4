package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostSummaryResponseDto {

    private static final String BLINDED_POST_TITLE = "숨김 처리된 게시글";

    @JsonProperty("post_id")
    private Long postId;

    private String title;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("like_count")
    private long likeCount;

    @JsonProperty("comment_count")
    private long commentCount;

    @JsonProperty("view_count")
    private long viewCount;

    @JsonProperty("is_edited")
    private boolean edited;

    private WriterResponseDto writer;

    private PollResponseDto poll;

    public PostSummaryResponseDto(Post post, long likeCount, long commentCount, boolean edited) {
        this(post, likeCount, commentCount, edited, post.getThumbnailUrl(), null);
    }

    public PostSummaryResponseDto(Post post, long likeCount, long commentCount, boolean edited, String thumbnailUrl) {
        this(post, likeCount, commentCount, edited, thumbnailUrl, null);
    }

    public PostSummaryResponseDto(
            Post post,
            long likeCount,
            long commentCount,
            boolean edited,
            String thumbnailUrl,
            PollResponseDto poll
    ) {
        this.postId = post.getId();
        this.title = post.isBlinded() ? BLINDED_POST_TITLE : post.getTitle();
        this.thumbnailUrl = post.isBlinded() ? null : thumbnailUrl;
        this.createdAt = post.getCreatedAt();
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = post.getViewCount();
        this.edited = edited;
        this.writer = new WriterResponseDto(post.getAuthor());
        this.poll = post.isBlinded() ? null : poll;
    }
}
