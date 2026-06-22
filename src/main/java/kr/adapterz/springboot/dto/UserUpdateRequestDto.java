package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequestDto {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 10, message = "닉네임은 10글자 이하여야 합니다.")
    @Pattern(regexp = "^\\S+$", message = "닉네임에는 띄어쓰기를 사용할 수 없습니다.")
    private String nickname;

    @JsonProperty("profile_image")
    private String profileImage;
}
