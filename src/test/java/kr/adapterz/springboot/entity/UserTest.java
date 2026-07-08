package kr.adapterz.springboot.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    @Test
    @DisplayName("회원 생성 시 이메일, 비밀번호, 닉네임, 프로필 이미지가 저장된다.")
    void createUser() {
        // given
        String email = "test@example.com";
        String password = "encodedPassword";
        String nickname = "tester";
        String profileImage = "profile.png";

        // when
        User user = User.of(email, password, nickname, profileImage);

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getProfileImage()).isEqualTo(profileImage);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("회원을 삭제하면 deletedAt이 기록되고 삭제 상태가 된다")
    void deleteUser() {
        // given
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "tester",
                "profile.png"
        );

        // when
        user.delete();

        // then
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("닉네임 수정을 하면 수정한 닉네임으로 저장된다")
    void changeNickname() {
        //given
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "tester",
                "profile.png"
        );

        String newNickname = "newNick";

        //when
        user.changeNickname(newNickname);

        //then
        assertThat(user.getNickname()).isEqualTo(newNickname);
    }

    @Test
    @DisplayName("프로필 이미지를 수정하면 수정한 프로필 이미지로 저장된다")
    void chageProfileImage() {
        //given
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "tester",
                "profile.png"
        );

        String newProfile = "new.png";

        //when
        user.changeProfileImage(newProfile);

        //then
        assertThat(user.getProfileImage()).isEqualTo(newProfile);
    }

    @Test
    @DisplayName("비밀번호를 수정하면 수정한 비밀번호로 저장된다")
    void changePassword() {
        //given
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "tester",
                "profile.png"
        );

        String newPassword = "newEncodedPassword";

        //when
        user.changePassword(newPassword);

        //then
        assertThat(user.getPassword()).isEqualTo(newPassword);
    }
}
