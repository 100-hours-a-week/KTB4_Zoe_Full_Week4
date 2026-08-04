package kr.adapterz.springboot.controller;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.auth.JwtTokenProvider;
import kr.adapterz.springboot.dto.MultipartPostCreateRequestDto;
import kr.adapterz.springboot.dto.PostCreateResponseDto;
import kr.adapterz.springboot.entity.Poll;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.GlobalExceptionHandler;
import kr.adapterz.springboot.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private ViewerKeyResolver viewerKeyResolver;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("반복된 poll_options 필드로 투표 포함 게시글 생성을 요청한다")
    void createPostWithPoll() throws Exception {
        PostCreateResponseDto response = createResponse(1L);
        given(postService.createPost(argThat(request ->
                request.getPollOptions().equals(java.util.List.of("Java", "Kotlin"))
        ))).willReturn(response);

        mockMvc.perform(multipart("/posts")
                        .param("title", "개발 언어 설문")
                        .param("content", "선호하는 개발 언어를 선택해주세요.")
                        .param("poll_options", "Java", "Kotlin"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("post_created"))
                .andExpect(jsonPath("$.data.post_id").value(1L))
                .andExpect(jsonPath("$.data.poll_id").value(1L));
    }

    @Test
    @DisplayName("투표 선택지가 누락된 게시글 생성 요청은 실패한다")
    void rejectPostWithoutPollOptions() throws Exception {
        mockMvc.perform(multipart("/posts")
                        .param("title", "개발 언어 설문")
                        .param("content", "선호하는 개발 언어를 선택해주세요."))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation_failed"))
                .andExpect(jsonPath("$.data.poll_options").value("선택지는 필수입니다."));

        then(postService).shouldHaveNoInteractions();
    }

    private PostCreateResponseDto createResponse(Long id) {
        User author = User.of("author@example.com", "encodedPassword", "author", null);
        Post post = new Post("개발 언어 설문", "본문", author);
        Poll poll = new Poll(post, java.util.List.of("Java", "Kotlin"));
        ReflectionTestUtils.setField(post, "id", id);
        ReflectionTestUtils.setField(poll, "postId", id);
        return new PostCreateResponseDto(post, poll);
    }
}
