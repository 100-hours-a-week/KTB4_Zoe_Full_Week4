package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PollVoteRequestDto {

    @NotNull(message = "선택지는 필수입니다.")
    @JsonProperty("option_id")
    private Long option_id;

    public Long getOptionId() {
        return option_id;
    }
}
