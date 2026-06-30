package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class PostListResponseDto {

    private List<PostSummaryResponseDto> posts;

    @JsonProperty("next_cursor")
    private Long nextCursor;

    @JsonProperty("has_next")
    private boolean hasNext;

    public PostListResponseDto(List<PostSummaryResponseDto> posts, Long nextCursor, boolean hasNext) {
        this.posts = posts;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }
}
