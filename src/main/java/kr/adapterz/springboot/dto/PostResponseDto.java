package kr.adapterz.springboot.dto;

import kr.adapterz.springboot.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostResponseDto {

    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private long commentCount;
    private long likeCount;
    private long viewCount;
    private boolean liked;

    public PostResponseDto(
            Post post,
            long commentCount,
            long likeCount,
            boolean liked
    ) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorId = post.getAuthor().getId();
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.viewCount = post.getViewCount();
        this.liked = liked;
    }
}