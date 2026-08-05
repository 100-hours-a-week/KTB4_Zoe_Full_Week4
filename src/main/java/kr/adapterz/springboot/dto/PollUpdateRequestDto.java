package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.adapterz.springboot.validation.ValidPollUpdateOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PollUpdateRequestDto {

    @NotNull(message = "선택지는 필수입니다.")
    @Size(min = 2, max = 5, message = "선택지는 2개 이상 5개 이하여야 합니다.")
    @ValidPollUpdateOptions
    @Valid
    private List<Option> options;

    @Getter
    @NoArgsConstructor
    public static class Option {

        @JsonProperty("option_id")
        private Long optionId;

        @NotBlank(message = "선택지는 필수입니다.")
        private String content;
    }
}
