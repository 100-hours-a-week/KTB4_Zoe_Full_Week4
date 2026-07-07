package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("자기 자신의 닉네임은 중복으로 판단하지 않는다")
    void existsByNicknameAndIdNotFalseBySameUser() {
        // given
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "tester",
                "profile.png"
        );
        User savedUser = userRepository.saveAndFlush(user);

        // when
        boolean exists = userRepository.existsByNicknameAndIdNot(
                "tester",
                savedUser.getId()
        );

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("다른 회원이 사용 중인 닉네임은 중복으로 판단한다")
    void existsByNicknameAndIdNotTrueByOtherUser() {
        // given
        User user1 = User.of(
                "test1@example.com",
                "encodedPassword",
                "tester",
                "profile1.png"
        );
        User user2 = User.of(
                "test2@example.com",
                "encodedPassword",
                "other",
                "profile2.png"
        );

        userRepository.save(user1);
        User savedUser2 = userRepository.saveAndFlush(user2);

        // when
        boolean exists = userRepository.existsByNicknameAndIdNot(
                "tester",
                savedUser2.getId()
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("중복된 이메일로 회원을 저장하면 예외가 발생한다")
    void saveFailByDuplicateEmail() {
        // given
        User user1 = User.of(
                "test@example.com",
                "encodedPassword",
                "tester1",
                "profile1.png"
        );
        User user2 = User.of(
                "test@example.com",
                "encodedPassword",
                "tester2",
                "profile2.png"
        );

        userRepository.saveAndFlush(user1);

        // when & then
        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("중복된 닉네임으로 회원을 저장하면 예외가 발생한다")
    void saveFailByDuplicateNickname() {
        // given
        User user1 = User.of(
                "test1@example.com",
                "encodedPassword",
                "tester",
                "profile1.png"
        );
        User user2 = User.of(
                "test2@example.com",
                "encodedPassword",
                "tester",
                "profile2.png"
        );

        userRepository.saveAndFlush(user1);

        // when & then
        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
