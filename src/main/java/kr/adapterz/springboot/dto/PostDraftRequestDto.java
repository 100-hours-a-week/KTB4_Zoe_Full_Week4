package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.validation.ValidPollOptions;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostDraftRequestDto {

    @JsonProperty("post_id")
    private Long postId;

    @Size(max = 26, message = "제목은 26글자 이하여야 합니다.")
    private String title;

    private String content;

    @JsonProperty("poll_options")
    @ValidPollOptions
    private List<String> pollOptions;
}
