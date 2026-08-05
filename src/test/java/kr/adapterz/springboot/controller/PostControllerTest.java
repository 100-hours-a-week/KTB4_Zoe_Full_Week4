package kr.adapterz.springboot.controller;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.auth.JwtTokenProvider;
import kr.adapterz.springboot.dto.MultipartPostCreateRequestDto;
import kr.adapterz.springboot.dto.PollResponseDto;
import kr.adapterz.springboot.dto.PostCreateResponseDto;
import kr.adapterz.springboot.dto.PostDetailResponseDto;
import kr.adapterz.springboot.dto.PostUpdateResponseDto;
import kr.adapterz.springboot.dto.PollResponseDto;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    @DisplayName("게시글 수정 요청의 JSON poll 파트를 서비스에 전달한다")
    void updatePostWithPollPart() throws Exception {
        User author = User.of("author@example.com", "encodedPassword", "author", null);
        Post post = new Post("수정 제목", "수정 본문", author);
        Poll poll = new Poll(post, java.util.List.of("Java", "Python"));
        ReflectionTestUtils.setField(post, "id", 1L);
        ReflectionTestUtils.setField(poll, "postId", 1L);
        PostUpdateResponseDto response = new PostUpdateResponseDto(
                post,
                PollResponseDto.withoutResult(poll, 0L)
        );
        given(postService.updatePost(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .willReturn(response);

        MockMultipartFile pollPart = new MockMultipartFile(
                "poll",
                "poll.json",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"options\":[{\"option_id\":101,\"content\":\"Java\"},{\"content\":\"Python\"}]}".getBytes()
        );

        mockMvc.perform(multipart("/posts/{postId}", 1L)
                        .file(pollPart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("title", "수정 제목")
                        .param("content", "수정 본문"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("post_updated"))
                .andExpect(jsonPath("$.data.poll.poll_id").value(1L));

        then(postService).should().updatePost(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.argThat(request -> request.getOptions().size() == 2)
        );
    }

    @Test
    @DisplayName("잘못된 JSON poll 파트는 400 응답을 반환한다")
    void rejectMalformedPollJson() throws Exception {
        MockMultipartFile malformedPoll = new MockMultipartFile(
                "poll",
                "poll.json",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"options\":[".getBytes()
        );

        mockMvc.perform(multipart("/posts/{postId}", 1L)
                        .file(malformedPoll)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("title", "수정 제목")
                        .param("content", "수정 본문"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid_request_body"));

        then(postService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("미참여 게시글 상세 응답에는 기본 투표 정보만 포함한다")
    void getPostWithPollWithoutResult() throws Exception {
        User author = User.of("author@example.com", "encodedPassword", "author", null);
        Post post = new Post("개발 언어 설문", "본문", author);
        Poll poll = new Poll(post, java.util.List.of("Java", "Kotlin"));
        ReflectionTestUtils.setField(author, "id", 2L);
        ReflectionTestUtils.setField(post, "id", 1L);
        ReflectionTestUtils.setField(poll, "postId", 1L);
        ReflectionTestUtils.setField(poll.getOptions().get(0), "id", 101L);
        ReflectionTestUtils.setField(poll.getOptions().get(1), "id", 102L);
        PollResponseDto pollResponse = PollResponseDto.withoutResult(poll, 3L);
        PostDetailResponseDto response = new PostDetailResponseDto(
                post,
                0L,
                0L,
                false,
                false,
                pollResponse
        );
        given(viewerKeyResolver.createGuestViewerKey(org.mockito.ArgumentMatchers.any()))
                .willReturn("GUEST:key");
        given(postService.getPost(1L, "GUEST:key")).willReturn(response);

        mockMvc.perform(get("/posts/{postId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.poll.poll_id").value(1L))
                .andExpect(jsonPath("$.data.poll.options[0].option_id").value(101L))
                .andExpect(jsonPath("$.data.poll.options[0].content").value("Java"))
                .andExpect(jsonPath("$.data.poll.has_voted").value(false))
                .andExpect(jsonPath("$.data.poll.total_vote_count").value(3L))
                .andExpect(jsonPath("$.data.poll.selected_option_id").doesNotExist())
                .andExpect(jsonPath("$.data.poll.result").doesNotExist());
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
