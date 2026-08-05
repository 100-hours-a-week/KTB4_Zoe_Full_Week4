package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Post;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@Getter
public class PostUpdateResponseDto {

    @JsonProperty("post_id")
    private Long postId;

    private String title;
    private String content;

    @JsonProperty("image_urls")
    private List<String> imageUrls;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private PollResponseDto poll;

    public PostUpdateResponseDto(Post post, PollResponseDto poll) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.imageUrls = post.getImages().stream()
                .map(image -> image.getImageUrl())
                .toList();
        this.poll = poll;
    }
}
