package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Post;
import lombok.Getter;

@Getter
public class PostCreateResponseDto {

    @JsonProperty("post_id")
    private Long postId;

    public PostCreateResponseDto(Post post) {
        this.postId = post.getId();
    }
}
