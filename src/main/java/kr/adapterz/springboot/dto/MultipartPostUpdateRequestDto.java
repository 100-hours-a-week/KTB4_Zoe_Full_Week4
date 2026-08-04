package kr.adapterz.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MultipartPostUpdateRequestDto {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 26, message = "제목은 26글자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "본문은 필수입니다.")
    private String content;

    private List<MultipartFile> images = new ArrayList<>();
}
