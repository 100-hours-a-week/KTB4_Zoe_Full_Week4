package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.auth.UnauthorizedException;
import kr.adapterz.springboot.dto.PollResultOptionResponseDto;
import kr.adapterz.springboot.dto.PostDetailResponseDto;
import kr.adapterz.springboot.entity.Poll;
import kr.adapterz.springboot.entity.PollVote;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.repository.PollRepository;
import kr.adapterz.springboot.repository.PollVoteRepository;
import kr.adapterz.springboot.repository.PostRepository;
import kr.adapterz.springboot.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
class PostDetailPollIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollVoteRepository pollVoteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    @DisplayName("레거시 게시글은 투표 없이 상세 조회한다")
    void getLegacyPostWithoutPoll() {
        User author = saveUser("author@example.com", "author");
        Post post = postRepository.saveAndFlush(new Post("레거시 게시글", "본문", author));
        given(currentUserProvider.getCurrentUserId()).willThrow(new UnauthorizedException());

        PostDetailResponseDto response = postService.getPost(post.getId(), "GUEST:legacy");

        assertThat(response.getPoll()).isNull();
    }

    @Test
    @DisplayName("비로그인 사용자는 전체 참여 인원만 포함한 기본 투표 정보를 조회한다")
    void getPollWithoutResultAsGuest() throws Exception {
        User author = saveUser("author@example.com", "author");
        User firstVoter = saveUser("first@example.com", "first");
        User secondVoter = saveUser("second@example.com", "second");
        Post post = savePostWithPoll(author);
        Poll poll = pollRepository.findByPostIdWithOptions(post.getId()).orElseThrow();
        saveVote(poll, firstVoter, 0);
        saveVote(poll, secondVoter, 1);
        given(currentUserProvider.getCurrentUserId()).willThrow(new UnauthorizedException());

        PostDetailResponseDto response = postService.getPost(post.getId(), "GUEST:poll");
        String pollJson = objectMapper.writeValueAsString(response.getPoll());

        assertThat(response.getPoll().isHasVoted()).isFalse();
        assertThat(response.getPoll().getTotalVoteCount()).isEqualTo(2);
        assertThat(response.getPoll().getOptions())
                .extracting(option -> option.getContent())
                .containsExactly("Java", "Kotlin");
        assertThat(pollJson)
                .doesNotContain("selected_option_id")
                .doesNotContain("result");
    }

    @Test
    @DisplayName("작성자도 참여하지 않았다면 선택지별 결과를 조회할 수 없다")
    void hideResultFromAuthorWhoHasNotVoted() throws Exception {
        User author = saveUser("author@example.com", "author");
        User voter = saveUser("voter@example.com", "voter");
        Post post = savePostWithPoll(author);
        Poll poll = pollRepository.findByPostIdWithOptions(post.getId()).orElseThrow();
        saveVote(poll, voter, 0);
        given(currentUserProvider.getCurrentUserId()).willReturn(author.getId());

        PostDetailResponseDto response = postService.getPost(post.getId(), "ignored");
        String pollJson = objectMapper.writeValueAsString(response.getPoll());

        assertThat(response.getPoll().isHasVoted()).isFalse();
        assertThat(response.getPoll().getTotalVoteCount()).isOne();
        assertThat(pollJson)
                .doesNotContain("selected_option_id")
                .doesNotContain("result");
    }

    @Test
    @DisplayName("참여 사용자는 현재 선택과 선택지별 최신 결과를 조회한다")
    void exposeResultToCurrentVoter() {
        User author = saveUser("author@example.com", "author");
        User currentVoter = saveUser("current@example.com", "current");
        User secondVoter = saveUser("second@example.com", "second");
        User thirdVoter = saveUser("third@example.com", "third");
        Post post = savePostWithPoll(author);
        Poll poll = pollRepository.findByPostIdWithOptions(post.getId()).orElseThrow();
        saveVote(poll, currentVoter, 0);
        saveVote(poll, secondVoter, 0);
        saveVote(poll, thirdVoter, 1);
        given(currentUserProvider.getCurrentUserId()).willReturn(currentVoter.getId());

        PostDetailResponseDto response = postService.getPost(post.getId(), "ignored");

        assertThat(response.getPoll().isHasVoted()).isTrue();
        assertThat(response.getPoll().getTotalVoteCount()).isEqualTo(3);
        assertThat(response.getPoll().getSelectedOptionId())
                .isEqualTo(poll.getOptions().get(0).getId());
        assertThat(response.getPoll().getResult().getOptions())
                .extracting(
                        PollResultOptionResponseDto::getVoteCount,
                        PollResultOptionResponseDto::getVoteRate
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(2L, "66.67"),
                        org.assertj.core.groups.Tuple.tuple(1L, "33.33")
                );
    }

    private User saveUser(String email, String nickname) {
        return userRepository.save(User.of(email, "encodedPassword", nickname, null));
    }

    private Post savePostWithPoll(User author) {
        Post post = postRepository.saveAndFlush(new Post("개발 언어 설문", "본문", author));
        pollRepository.saveAndFlush(new Poll(post, List.of("Java", "Kotlin")));
        return post;
    }

    private void saveVote(Poll poll, User user, int optionIndex) {
        pollVoteRepository.persist(new PollVote(poll, user, poll.getOptions().get(optionIndex)));
    }
}
