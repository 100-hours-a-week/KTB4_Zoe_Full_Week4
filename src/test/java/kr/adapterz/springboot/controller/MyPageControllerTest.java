package kr.adapterz.springboot.controller;

import kr.adapterz.springboot.dto.MyPageActivitiesQuery;
import kr.adapterz.springboot.dto.MyPageActivityItemResponseDto;
import kr.adapterz.springboot.dto.MyPageActivityResponseDto;
import kr.adapterz.springboot.dto.MyPageProfileResponseDto;
import kr.adapterz.springboot.dto.MyPageQuery;
import kr.adapterz.springboot.dto.MyPageResponseDto;
import kr.adapterz.springboot.dto.MyPageStatsResponseDto;
import kr.adapterz.springboot.auth.JwtTokenProvider;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.GlobalExceptionHandler;
import kr.adapterz.springboot.exception.InvalidCursorException;
import kr.adapterz.springboot.exception.DeletedUserException;
import kr.adapterz.springboot.service.MyPageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MyPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyPageService myPageService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("마이페이지 최초 조회는 기본 탭과 기본 크기를 서비스에 전달한다")
    void getMyPageWithDefaults() throws Exception {
        given(myPageService.getMyPage(any(MyPageQuery.class)))
                .willReturn(myPageResponse("written"));

        mockMvc.perform(get("/users/me/mypage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("fetch_success"))
                .andExpect(jsonPath("$.data.activity.tab").value("written"))
                .andExpect(jsonPath("$.data.activity.items[0].post_id").value(101))
                .andExpect(jsonPath("$.data.activity.items[0].activity_at")
                        .value("2026-08-06 10:20:00"))
                .andExpect(jsonPath("$.data.activity.items[0].poll_status").doesNotExist());

        then(myPageService).should().getMyPage(any(MyPageQuery.class));
    }

    @Test
    @DisplayName("활동 목록 조회는 탭과 페이지 파라미터를 서비스에 전달한다")
    void getActivities() throws Exception {
        given(myPageService.getActivities(any(MyPageActivitiesQuery.class)))
                .willReturn(activityResponse("liked"));

        mockMvc.perform(get("/users/me/mypage/activities")
                        .param("tab", "liked")
                        .param("size", "10")
                        .param("cursor", "cursor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("fetch_success"))
                .andExpect(jsonPath("$.data.tab").value("liked"))
                .andExpect(jsonPath("$.data.items").isArray());

        then(myPageService).should().getActivities(any(MyPageActivitiesQuery.class));
    }

    @Test
    @DisplayName("지원하지 않는 활동 탭은 필드 맵 검증 오류를 반환한다")
    void rejectInvalidTab() throws Exception {
        mockMvc.perform(get("/users/me/mypage")
                        .param("tab", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation_failed"))
                .andExpect(jsonPath("$.data.tab").exists());

        then(myPageService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("유효하지 않은 Cursor는 cursor 필드 오류를 반환한다")
    void rejectInvalidCursor() throws Exception {
        given(myPageService.getActivities(any(MyPageActivitiesQuery.class)))
                .willThrow(new InvalidCursorException());

        mockMvc.perform(get("/users/me/mypage/activities")
                        .param("tab", "written")
                        .param("cursor", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid_cursor"))
                .andExpect(jsonPath("$.data.cursor").value("invalid_cursor"));
    }

    @Test
    @DisplayName("탈퇴한 사용자의 마이페이지 요청은 오류 필드 맵을 반환한다")
    void rejectDeletedUser() throws Exception {
        given(myPageService.getMyPage(any(MyPageQuery.class)))
                .willThrow(new DeletedUserException());

        mockMvc.perform(get("/users/me/mypage"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("user_deleted"))
                .andExpect(jsonPath("$.data.error").value("user_deleted"));
    }

    private MyPageResponseDto myPageResponse(String tab) {
        return new MyPageResponseDto(
                new MyPageProfileResponseDto(user()),
                new MyPageStatsResponseDto(1, 2, 3),
                activityResponse(tab)
        );
    }

    private MyPageActivityResponseDto activityResponse(String tab) {
        return new MyPageActivityResponseDto(
                tab,
                List.of(new MyPageActivityItemResponseDto(
                        101L,
                        "테스트 게시글",
                        LocalDateTime.of(2026, 8, 6, 10, 20),
                        LocalDateTime.of(2026, 8, 6, 10, 20),
                        3,
                        4,
                        5,
                        false
                )),
                null,
                false
        );
    }

    private User user() {
        User user = User.of("test@example.com", "password", "tester", null);
        ReflectionTestUtils.setField(user, "id", 42L);
        return user;
    }
}
