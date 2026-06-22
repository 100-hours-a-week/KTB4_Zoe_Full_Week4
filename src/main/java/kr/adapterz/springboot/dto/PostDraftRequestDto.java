package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostDraftRequestDto {

    @JsonProperty("post_id")
    private Long postId;

    private String title;

    private String content;
}
