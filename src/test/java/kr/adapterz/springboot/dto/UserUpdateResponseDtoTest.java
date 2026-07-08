package kr.adapterz.springboot.dto;

import kr.adapterz.springboot.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class UserUpdateResponseDtoTest {

    @Test
    @DisplayName("User 엔티티로부터 회원 수정 응답 DTO를 생성할 수 있다")
    void createUserUpdateResponseDto() {
        // given
        Long userId = 1L;
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "tester",
                "profile.png"
        );
        ReflectionTestUtils.setField(user, "id", userId);

        // when
        UserUpdateResponseDto response = new UserUpdateResponseDto(user);

        // then
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getNickname()).isEqualTo("tester");
        assertThat(response.getProfileImage()).isEqualTo("profile.png");
    }
}