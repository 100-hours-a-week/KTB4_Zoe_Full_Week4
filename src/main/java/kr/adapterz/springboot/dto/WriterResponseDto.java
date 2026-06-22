package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.User;
import lombok.Getter;

@Getter
public class WriterResponseDto {

    private static final String DELETED_USER_NICKNAME = "알 수 없음";

    @JsonProperty("user_id")
    private Long userId;

    private String nickname;

    @JsonProperty("profile_image")
    private String profileImage;

    public WriterResponseDto(User user) {
        this.userId = user.getId();

        if (user.isDeleted()) {
            this.nickname = DELETED_USER_NICKNAME;
            this.profileImage = null;
            return;
        }

        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
    }
}
