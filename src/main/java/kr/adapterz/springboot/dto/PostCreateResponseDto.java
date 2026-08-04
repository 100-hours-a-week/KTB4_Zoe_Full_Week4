package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Poll;
import kr.adapterz.springboot.entity.Post;
import lombok.Getter;

@Getter
public class PostCreateResponseDto {

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("poll_id")
    private Long pollId;

    public PostCreateResponseDto(Post post, Poll poll) {
        this.postId = post.getId();
        this.pollId = poll.getPostId();
    }
}
