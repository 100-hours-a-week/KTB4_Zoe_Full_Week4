package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.PollOption;
import lombok.Getter;

@Getter
public class PollOptionResponseDto {

    @JsonProperty("option_id")
    private final Long optionId;

    private final String content;

    public PollOptionResponseDto(PollOption option) {
        this.optionId = option.getId();
        this.content = option.getContent();
    }
}
