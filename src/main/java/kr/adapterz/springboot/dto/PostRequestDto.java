package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostRequestDto {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 26, message = "제목은 26글자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "본문은 필수입니다.")
    private String content;

    @JsonProperty("image_urls")
    private List<@Size(max = 255, message = "이미지 URL은 255자 이하여야 합니다.") String> imageUrls = List.of();
}
