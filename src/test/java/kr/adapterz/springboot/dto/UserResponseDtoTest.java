package kr.adapterz.springboot.dto;

import kr.adapterz.springboot.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class UserResponseDtoTest {

    @Test
    @DisplayName("User 엔티티로부터 응답 DTO를 생성할 수 있다")
    void createUserResponseDto() {
        // given
        User user = User.of("test@example.com", "password", "tester", "profile.png");
        ReflectionTestUtils.setField(user, "id", 1L);

        // when
        UserResponseDto response = new UserResponseDto(user);

        // then
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("tester");
        assertThat(response.getProfileImage()).isEqualTo("profile.png");
    }
}
