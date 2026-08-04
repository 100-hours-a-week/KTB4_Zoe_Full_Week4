package kr.adapterz.springboot.controller;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.auth.JwtTokenProvider;
import kr.adapterz.springboot.dto.PollVoteRequestDto;
import kr.adapterz.springboot.dto.PollVoteCancelResponseDto;
import kr.adapterz.springboot.dto.PollVoteUpdateResponseDto;
import kr.adapterz.springboot.entity.Poll;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.GlobalExceptionHandler;
import kr.adapterz.springboot.exception.PollNotFoundException;
import kr.adapterz.springboot.exception.PollOptionMismatchException;
import kr.adapterz.springboot.repository.PollVoteCountProjection;
import kr.adapterz.springboot.service.PollVoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PollVoteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PollVoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PollVoteService pollVoteService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("투표 참여 또는 선택 변경 요청에 최신 결과를 반환한다")
    void updatePollVote() throws Exception {
        given(pollVoteService.vote(eq(1L), any(PollVoteRequestDto.class)))
                .willReturn(createResponse());

        mockMvc.perform(put("/posts/{postId}/poll/vote", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"option_id\":101}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("poll_vote_updated"))
                .andExpect(jsonPath("$.data.poll_id").value(1L))
                .andExpect(jsonPath("$.data.selected_option_id").value(101L))
                .andExpect(jsonPath("$.data.result.total_vote_count").value(1L))
                .andExpect(jsonPath("$.data.result.options[0].vote_count").value(1L))
                .andExpect(jsonPath("$.data.result.options[0].vote_rate").value("100.00"));
    }

    @Test
    @DisplayName("선택지가 누락된 요청은 검증에 실패한다")
    void rejectMissingOptionId() throws Exception {
        mockMvc.perform(put("/posts/{postId}/poll/vote", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation_failed"))
                .andExpect(jsonPath("$.data.option_id").value("선택지는 필수입니다."));

        then(pollVoteService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("존재하지 않는 투표는 404 응답을 반환한다")
    void returnNotFoundForMissingPoll() throws Exception {
        given(pollVoteService.vote(eq(999L), any(PollVoteRequestDto.class)))
                .willThrow(new PollNotFoundException());

        mockMvc.perform(put("/posts/{postId}/poll/vote", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"option_id\":101}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("poll_not_found"));
    }

    @Test
    @DisplayName("다른 투표의 선택지는 400 응답을 반환한다")
    void returnBadRequestForMismatchedOption() throws Exception {
        given(pollVoteService.vote(eq(1L), any(PollVoteRequestDto.class)))
                .willThrow(new PollOptionMismatchException());

        mockMvc.perform(put("/posts/{postId}/poll/vote", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"option_id\":999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("poll_option_mismatch"));
    }

    @Test
    @DisplayName("투표 참여 취소 후 미참여 상태와 전체 참여 인원을 반환한다")
    void cancelPollVote() throws Exception {
        given(pollVoteService.cancelVote(1L))
                .willReturn(new PollVoteCancelResponseDto(1L, 2L));

        mockMvc.perform(delete("/posts/{postId}/poll/vote", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("poll_vote_cancelled"))
                .andExpect(jsonPath("$.data.poll_id").value(1L))
                .andExpect(jsonPath("$.data.has_voted").value(false))
                .andExpect(jsonPath("$.data.total_vote_count").value(2L))
                .andExpect(jsonPath("$.data.selected_option_id").doesNotExist())
                .andExpect(jsonPath("$.data.result").doesNotExist());
    }

    private PollVoteUpdateResponseDto createResponse() {
        User author = User.of("author@example.com", "encodedPassword", "author", null);
        Post post = new Post("투표 게시글", "본문", author);
        Poll poll = new Poll(post, List.of("Java", "Kotlin"));
        ReflectionTestUtils.setField(post, "id", 1L);
        ReflectionTestUtils.setField(poll, "postId", 1L);
        ReflectionTestUtils.setField(poll.getOptions().get(0), "id", 101L);
        ReflectionTestUtils.setField(poll.getOptions().get(1), "id", 102L);

        return new PollVoteUpdateResponseDto(
                poll,
                101L,
                1L,
                List.of(count(1L, 101L, 1L))
        );
    }

    private PollVoteCountProjection count(Long pollId, Long optionId, long voteCount) {
        return new PollVoteCountProjection() {
            @Override
            public Long getPollId() {
                return pollId;
            }

            @Override
            public Long getOptionId() {
                return optionId;
            }

            @Override
            public long getVoteCount() {
                return voteCount;
            }
        };
    }
}
