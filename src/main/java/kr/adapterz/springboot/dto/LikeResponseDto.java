package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class LikeResponseDto {

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("like_count")
    private long likeCount;

    @JsonProperty("is_liked")
    private boolean liked;

    public LikeResponseDto(Long postId, long likeCount, boolean liked) {
        this.postId = postId;
        this.likeCount = likeCount;
        this.liked = liked;
    }
}
