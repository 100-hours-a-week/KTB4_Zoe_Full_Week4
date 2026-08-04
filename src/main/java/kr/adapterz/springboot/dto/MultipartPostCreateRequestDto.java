package kr.adapterz.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.adapterz.springboot.validation.ValidPollOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class MultipartPostCreateRequestDto {

    @Getter
    @Setter
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 26, message = "제목은 26글자 이하여야 합니다.")
    private String title;

    @Getter
    @Setter
    @NotBlank(message = "본문은 필수입니다.")
    private String content;

    @Getter
    @Setter
    private List<MultipartFile> images = new ArrayList<>();

    @NotNull(message = "선택지는 필수입니다.")
    @Size(min = 2, max = 5, message = "선택지는 2개 이상 5개 이하여야 합니다.")
    @ValidPollOptions
    private List<String> poll_options;

    public List<String> getPollOptions() {
        return poll_options;
    }

    public void setPoll_options(List<String> pollOptions) {
        this.poll_options = pollOptions;
    }
}
