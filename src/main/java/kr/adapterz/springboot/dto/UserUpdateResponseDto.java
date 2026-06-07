package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.User;
import lombok.Getter;

@Getter
public class UserUpdateResponseDto {

    @JsonProperty("user_id")
    private Long userId;

    private String nickname;

    @JsonProperty("profile_image")
    private String profileImage;

    public UserUpdateResponseDto(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
    }
}
