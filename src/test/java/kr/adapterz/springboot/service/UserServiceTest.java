package kr.adapterz.springboot.service;

import kr.adapterz.springboot.dto.MultipartUserUpdateRequestDto;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.DeletedUserException;
import kr.adapterz.springboot.exception.DuplicateNicknameException;
import kr.adapterz.springboot.exception.UserNotFoundException;
import kr.adapterz.springboot.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원 ID로 활성 회원을 조회할 수 있다.")
    void getUser() {
        // given
        Long userId = 1L;
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "tester",
                "profile.png"
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        User foundUser = userService.getUser(userId);

        // then
        assertThat(foundUser).isSameAs(user);
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getNickname()).isEqualTo("tester");

        then(userRepository).should().findById(userId);
    }

    @Test
    @DisplayName("회원 ID로 조회했을 때 회원이 없으면 예외가 발생한다")
    void getUserFailByNotFound() {
        // given
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(UserNotFoundException.class);

        // then
        then(userRepository).should().findById(userId);
    }

    @Test
    @DisplayName("탈퇴한 회원을 조회하면 예외가 발생한다")
    void getUserFailByDeletedUser() {

        // given
        Long userId = 1L;
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "tester",
                "profile.png"
        );
        user.delete();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(DeletedUserException.class);

        // then
        then(userRepository).should().findById(userId);
    }

    @Test
    @DisplayName("회원 정보를 수정할 수 있다")
    void updateUser() {
        // given
        Long userId = 1L;
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "oldNick",
                "old.png"
        );

        ReflectionTestUtils.setField(user, "id", userId);
        MockMultipartFile profileImage = new MockMultipartFile(
                "profile_image",
                "profile.png",
                "image/png",
                "image".getBytes()
        );

        MultipartUserUpdateRequestDto request = new MultipartUserUpdateRequestDto();
        request.setNickname("newNick");
        request.setProfileImage(profileImage);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNicknameAndIdNot("newNick", userId)).willReturn(false);
        given(imageStorageService.storeProfileImage(profileImage)).willReturn("/uploads/profile-images/stored.png");
        given(userRepository.save(user)).willReturn(user);

        // when
        User updatedUser = userService.updateUser(userId, request);

        // then
        assertThat(updatedUser.getNickname()).isEqualTo("newNick");
        assertThat(updatedUser.getProfileImage()).isEqualTo("/uploads/profile-images/stored.png");

        then(userRepository).should().findById(userId);
        then(userRepository).should().existsByNicknameAndIdNot("newNick", userId);
        then(imageStorageService).should().storeProfileImage(profileImage);
        then(userRepository).should().save(user);
    }

    @Test
    @DisplayName("중복된 닉네임으로 회원 정보를 수정하면 예외가 발생한다")
    void updateUserFailByDuplicateNickname() {
        // given
        Long userId = 1L;
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "oldNick",
                "old.png"
        );

        ReflectionTestUtils.setField(user, "id", userId);

        MockMultipartFile profileImage = new MockMultipartFile(
                "profile_image",
                "profile.png",
                "image/png",
                "image".getBytes()
        );

        MultipartUserUpdateRequestDto request = new MultipartUserUpdateRequestDto();
        ReflectionTestUtils.setField(request, "nickname", "duplicateNick");
        ReflectionTestUtils.setField(request, "profileImage", profileImage);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNicknameAndIdNot("duplicateNick", userId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(DuplicateNicknameException.class);

        then(userRepository).should().findById(userId);
        then(userRepository).should().existsByNicknameAndIdNot("duplicateNick", userId);
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원 정보를 수정하면 예외가 발생한다")
    void updateUserFailByNotFound() {
        // given
        Long userId = 1L;

        MockMultipartFile profileImage = new MockMultipartFile(
                "profile_image",
                "profile.png",
                "image/png",
                "image".getBytes()
        );

        MultipartUserUpdateRequestDto request = new MultipartUserUpdateRequestDto();
        ReflectionTestUtils.setField(request, "nickname", "newNick");
        ReflectionTestUtils.setField(request, "profileImage", profileImage);

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepository).should().findById(userId);
        then(userRepository).should(never()).existsByNicknameAndIdNot(any(), any());
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("탈퇴한 회원 정보를 수정하면 예외가 발생한다")
    void updateUserFailByDeletedUser() {
        // given
        Long userId = 1L;
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "tester",
                "profile.png"
        );
        ReflectionTestUtils.setField(user, "id", userId);
        user.delete();

        MockMultipartFile profileImage = new MockMultipartFile(
                "profile_image",
                "profile.png",
                "image/png",
                "image".getBytes()
        );

        MultipartUserUpdateRequestDto request = new MultipartUserUpdateRequestDto();
        ReflectionTestUtils.setField(request, "nickname", "newNick");
        ReflectionTestUtils.setField(request, "profileImage", profileImage);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(DeletedUserException.class);

        then(userRepository).should().findById(userId);
        then(userRepository).should(never()).existsByNicknameAndIdNot(any(), any());
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원을 탈퇴 처리할 수 있다")
    void deleteUser() {
        // given
        Long userId = 1L;
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "tester",
                "profile.png"
        );
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);

        // when
        userService.deleteUser(userId);

        // then
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.isDeleted()).isTrue();

        then(userRepository).should().findById(userId);
        then(userRepository).should().save(user);
    }

    @Test
    @DisplayName("이미 탈퇴한 회원을 다시 탈퇴 처리하면 예외가 발생한다")
    void deleteUserFailByDeletedUser() {
        // given
        Long userId = 1L;
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "tester",
                "profile.png"
        );
        ReflectionTestUtils.setField(user, "id", userId);
        user.delete();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(DeletedUserException.class);

        then(userRepository).should().findById(userId);
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원을 탈퇴 처리하면 예외가 발생한다")
    void deleteUserFailByNotFound() {
        // given
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepository).should().findById(userId);
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("프로필 이미지 파일과 함께 회원 정보를 수정할 수 있다")
    void updateUserWithProfileImage() {
        // given
        Long userId = 1L;
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "oldNick",
                "old.png"
        );
        ReflectionTestUtils.setField(user, "id", userId);

        MockMultipartFile profileImage = new MockMultipartFile(
                "profile_image",
                "profile.png",
                "image/png",
                "image".getBytes()
        );

        MultipartUserUpdateRequestDto request = new MultipartUserUpdateRequestDto();
        request.setNickname("newNick");
        request.setProfileImage(profileImage);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNicknameAndIdNot("newNick", userId)).willReturn(false);
        given(imageStorageService.storeProfileImage(profileImage)).willReturn("/uploads/profile-images/stored.png");
        given(userRepository.save(user)).willReturn(user);

        // when
        User updatedUser = userService.updateUser(userId, request);

        // then
        assertThat(updatedUser.getNickname()).isEqualTo("newNick");
        assertThat(updatedUser.getProfileImage()).isEqualTo("/uploads/profile-images/stored.png");

        then(userRepository).should().findById(userId);
        then(userRepository).should().existsByNicknameAndIdNot("newNick", userId);
        then(imageStorageService).should().storeProfileImage(profileImage);
        then(userRepository).should().save(user);
    }

    @Test
    @DisplayName("프로필 이미지 파일이 없으면 기존 프로필 이미지를 유지한다")
    void updateUserWithoutProfileImage() {
        // given
        Long userId = 1L;
        User user = User.of(
                "test@example.com",
                "encodedPassword",
                "oldNick",
                "old.png"
        );
        ReflectionTestUtils.setField(user, "id", userId);

        MultipartUserUpdateRequestDto request = new MultipartUserUpdateRequestDto();
        request.setNickname("newNick");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNicknameAndIdNot("newNick", userId)).willReturn(false);
        given(userRepository.save(user)).willReturn(user);

        // when
        User updatedUser = userService.updateUser(userId, request);

        // then
        assertThat(updatedUser.getNickname()).isEqualTo("newNick");
        assertThat(updatedUser.getProfileImage()).isEqualTo("old.png");

        then(userRepository).should().findById(userId);
        then(userRepository).should().existsByNicknameAndIdNot("newNick", userId);
        then(imageStorageService).should().storeProfileImage(null);
        then(userRepository).should().save(user);
    }

}
