package kr.adapterz.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class MultipartUserUpdateRequestDto {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 10, message = "닉네임은 10글자 이하여야 합니다.")
    @Pattern(regexp = "^\\S+$", message = "닉네임에는 띄어쓰기를 사용할 수 없습니다.")
    private String nickname;

    private MultipartFile profileImage;

    public void setProfile_image(MultipartFile profileImage) {
        this.profileImage = profileImage;
    }
}
