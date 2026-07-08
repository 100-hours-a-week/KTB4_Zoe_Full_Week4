package kr.adapterz.springboot.controller;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.auth.JwtTokenProvider;
import kr.adapterz.springboot.dto.MultipartUserUpdateRequestDto;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.DeletedUserException;
import kr.adapterz.springboot.exception.DuplicateNicknameException;
import kr.adapterz.springboot.exception.GlobalExceptionHandler;
import kr.adapterz.springboot.exception.UserNotFoundException;
import kr.adapterz.springboot.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("내 회원 정보를 조회할 수 있다")
    void getUser() throws Exception {
        // given
        Long userId = 1L;
        User user = createUser(userId, "test@example.com", "tester", "profile.png");

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(userService.getUser(userId)).willReturn(user);

        // when & then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("fetch_success"))
                .andExpect(jsonPath("$.data.user_id").value(userId))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.profile_image").value("profile.png"));

        then(currentUserProvider).should().getCurrentUserId();
        then(userService).should().getUser(userId);
    }

    @Test
    @DisplayName("존재하지 않는 내 회원 정보를 조회하면 404 응답을 반환한다")
    void getUserFailByNotFound() throws Exception {
        // given
        Long userId = 1L;

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(userService.getUser(userId)).willThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(get("/users"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("user_not_found"))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(currentUserProvider).should().getCurrentUserId();
        then(userService).should().getUser(userId);
    }

    @Test
    @DisplayName("탈퇴한 내 회원 정보를 조회하면 403 응답을 반환한다")
    void getUserFailByDeletedUser() throws Exception {
        // given
        Long userId = 1L;

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(userService.getUser(userId)).willThrow(new DeletedUserException());

        // when & then
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("user_deleted"))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(currentUserProvider).should().getCurrentUserId();
        then(userService).should().getUser(userId);
    }

    @Test
    @DisplayName("회원 정보를 수정할 수 있다")
    void putUser() throws Exception {
        // given
        Long userId = 1L;
        User updatedUser = createUser(userId, "test@example.com", "newNick", "new.png");

        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "profile.png",
                "image/png",
                "image".getBytes()
        );

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(userService.updateUser(eq(userId), any(MultipartUserUpdateRequestDto.class))).willReturn(updatedUser);

        // when & then
        mockMvc.perform(multipart(HttpMethod.PUT, "/users")
                        .file(profileImage)
                        .param("nickname", "newNick"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("user_updated"))
                .andExpect(jsonPath("$.data.user_id").value(userId))
                .andExpect(jsonPath("$.data.nickname").value("newNick"))
                .andExpect(jsonPath("$.data.profile_image").value("new.png"));

        then(currentUserProvider).should().getCurrentUserId();
        then(userService).should().updateUser(eq(userId), any(MultipartUserUpdateRequestDto.class));
    }


    @Test
    @DisplayName("닉네임이 10글자 이상이면 400 응답을 반환한다")
    void putUserWithImageFailByValidation() throws Exception {
        // when & then
        mockMvc.perform(multipart(HttpMethod.PUT, "/users")
                        .param("nickname", "tooLongNick1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation_failed"))
                .andExpect(jsonPath("$.data.nickname").value("닉네임은 10글자 이하여야 합니다."));

        then(currentUserProvider).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("중복된 닉네임으로 내 회원 정보를 수정하면 409 응답을 반환한다")
    void putUserFailByDuplicateNickname() throws Exception {
        // given
        Long userId = 1L;

        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "profile.png",
                "image/png",
                "image".getBytes()
        );

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(userService.updateUser(eq(userId), any(MultipartUserUpdateRequestDto.class)))
                .willThrow(new DuplicateNicknameException());

        // when & then
        mockMvc.perform(multipart(HttpMethod.PUT,"/users")
                        .file(profileImage)
                        .param("nickname","Nick"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("nickname_already_exists"))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(currentUserProvider).should().getCurrentUserId();
        then(userService).should().updateUser(eq(userId), any(MultipartUserUpdateRequestDto.class));
    }

    @Test
    @DisplayName("내 회원을 탈퇴 처리할 수 있다")
    void deleteUser() throws Exception {
        // given
        Long userId = 1L;

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);

        // when & then
        mockMvc.perform(delete("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("user_deleted"))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(currentUserProvider).should().getCurrentUserId();
        then(userService).should().deleteUser(userId);
    }

    private User createUser(Long userId, String email, String nickname, String profileImage) {
        User user = User.of(
                email,
                "encodedPassword",
                nickname,
                profileImage
        );
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
