package kr.adapterz.springboot.dto;

import kr.adapterz.springboot.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@NoArgsConstructor
public class PostResponseDto {

    @JsonProperty("post_id")
    private Long postId;
    private String title;
    private String content;
    @JsonProperty("author_id")
    private Long authorId;
    @JsonProperty("comment_count")
    private long commentCount;
    @JsonProperty("like_count")
    private long likeCount;
    @JsonProperty("view_count")
    private long viewCount;
    private boolean liked;

    public PostResponseDto(
            Post post,
            long commentCount,
            long likeCount,
            boolean liked
    ) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorId = post.getAuthor().getId();
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.viewCount = post.getViewCount();
        this.liked = liked;
    }
}
