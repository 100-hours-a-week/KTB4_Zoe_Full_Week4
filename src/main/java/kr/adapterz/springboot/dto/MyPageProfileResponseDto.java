package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.User;
import lombok.Getter;

@Getter
public class MyPageProfileResponseDto {

    @JsonProperty("user_id")
    private final Long userId;
    private final String nickname;
    private final String email;

    @JsonProperty("profile_image")
    private final String profileImage;

    public MyPageProfileResponseDto(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.profileImage = user.getProfileImage();
    }
}
