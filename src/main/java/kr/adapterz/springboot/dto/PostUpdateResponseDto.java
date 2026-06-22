package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Post;
import lombok.Getter;

import java.util.List;

@Getter
public class PostUpdateResponseDto {

    @JsonProperty("post_id")
    private Long postId;

    private String title;
    private String content;

    @JsonProperty("image_urls")
    private List<String> imageUrls;

    public PostUpdateResponseDto(Post post) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.imageUrls = post.getImages().stream()
                .map(image -> image.getImageUrl())
                .toList();
    }
}
