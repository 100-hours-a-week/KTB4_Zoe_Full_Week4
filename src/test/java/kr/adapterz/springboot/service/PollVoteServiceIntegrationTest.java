package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.PollVoteRequestDto;
import kr.adapterz.springboot.dto.PollVoteUpdateResponseDto;
import kr.adapterz.springboot.entity.Poll;
import kr.adapterz.springboot.entity.PollVote;
import kr.adapterz.springboot.entity.PollVoteId;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.DeletedUserException;
import kr.adapterz.springboot.exception.PollNotFoundException;
import kr.adapterz.springboot.exception.PollOptionMismatchException;
import kr.adapterz.springboot.repository.PollRepository;
import kr.adapterz.springboot.repository.PollVoteRepository;
import kr.adapterz.springboot.repository.PostRepository;
import kr.adapterz.springboot.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

@Tag("mysql")
@Testcontainers
@SpringBootTest
@TestPropertySource(properties = "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver")
class PollVoteServiceIntegrationTest {

    private static final int CONCURRENT_REQUEST_COUNT = 4;

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Autowired
    private PollVoteService pollVoteService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollVoteRepository pollVoteRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @BeforeEach
    void cleanDatabase() {
        pollVoteRepository.deleteAll();
        pollRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("로그인 사용자가 투표에 처음 참여한다")
    void createVote() {
        PollContext context = savePollContext();
        given(currentUserProvider.getCurrentUserId()).willReturn(context.voter().getId());

        PollVoteUpdateResponseDto response = pollVoteService.vote(
                context.poll().getPostId(),
                request(context.poll().getOptions().get(0).getId())
        );

        assertThat(response.getPollId()).isEqualTo(context.poll().getPostId());
        assertThat(response.getSelectedOptionId()).isEqualTo(context.poll().getOptions().get(0).getId());
        assertThat(response.getResult().getTotalVoteCount()).isOne();
        assertThat(response.getResult().getOptions())
                .extracting(option -> option.getVoteCount(), option -> option.getVoteRate())
                .containsExactly(tuple(1L, "100.00"), tuple(0L, "0.00"));
        assertThat(pollVoteRepository.count()).isOne();
    }

    @Test
    @DisplayName("기존 선택을 다른 선택지로 변경해도 전체 참여 인원은 유지한다")
    void changeVote() {
        PollContext context = savePollContext();
        given(currentUserProvider.getCurrentUserId()).willReturn(context.voter().getId());
        pollVoteService.vote(
                context.poll().getPostId(),
                request(context.poll().getOptions().get(0).getId())
        );

        PollVoteUpdateResponseDto response = pollVoteService.vote(
                context.poll().getPostId(),
                request(context.poll().getOptions().get(1).getId())
        );

        assertThat(response.getSelectedOptionId()).isEqualTo(context.poll().getOptions().get(1).getId());
        assertThat(response.getResult().getTotalVoteCount()).isOne();
        assertThat(response.getResult().getOptions())
                .extracting(option -> option.getVoteCount())
                .containsExactly(0L, 1L);
        assertThat(pollVoteRepository.count()).isOne();
    }

    @Test
    @DisplayName("같은 선택지를 다시 요청하면 투표 행을 변경하지 않고 성공한다")
    void keepSameVoteIdempotently() {
        PollContext context = savePollContext();
        Long pollId = context.poll().getPostId();
        Long userId = context.voter().getId();
        Long optionId = context.poll().getOptions().get(0).getId();
        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        pollVoteService.vote(pollId, request(optionId));
        LocalDateTime updatedAt = pollVoteRepository.findById(new PollVoteId(pollId, userId))
                .orElseThrow()
                .getUpdatedAt();

        pollVoteService.vote(pollId, request(optionId));
        PollVote unchangedVote = pollVoteRepository.findById(new PollVoteId(pollId, userId)).orElseThrow();

        assertThat(unchangedVote.getOptionId()).isEqualTo(optionId);
        assertThat(unchangedVote.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(pollVoteRepository.count()).isOne();
    }

    @Test
    @DisplayName("다른 투표의 선택지는 선택할 수 없다")
    void rejectOptionFromDifferentPoll() {
        PollContext context = savePollContext();
        Post otherPost = postRepository.saveAndFlush(new Post("다른 게시글", "본문", context.author()));
        Poll otherPoll = pollRepository.saveAndFlush(new Poll(otherPost, List.of("Python", "Go")));
        given(currentUserProvider.getCurrentUserId()).willReturn(context.voter().getId());

        assertThatThrownBy(() -> pollVoteService.vote(
                context.poll().getPostId(),
                request(otherPoll.getOptions().get(0).getId())
        )).isInstanceOf(PollOptionMismatchException.class);

        assertThat(pollVoteRepository.count()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 투표에는 참여할 수 없다")
    void rejectMissingPoll() {
        User voter = saveUser("voter@example.com", "voter");
        given(currentUserProvider.getCurrentUserId()).willReturn(voter.getId());

        assertThatThrownBy(() -> pollVoteService.vote(999L, request(999L)))
                .isInstanceOf(PollNotFoundException.class);
    }

    @Test
    @DisplayName("탈퇴한 사용자는 투표에 참여할 수 없다")
    void rejectDeletedUser() {
        PollContext context = savePollContext();
        context.voter().delete();
        userRepository.saveAndFlush(context.voter());
        given(currentUserProvider.getCurrentUserId()).willReturn(context.voter().getId());

        assertThatThrownBy(() -> pollVoteService.vote(
                context.poll().getPostId(),
                request(context.poll().getOptions().getFirst().getId())
        )).isInstanceOf(DeletedUserException.class);

        assertThat(pollVoteRepository.count()).isZero();
    }

    @Test
    @DisplayName("동일 사용자의 동시 최초 참여는 복합 기본키로 하나의 현재 투표만 유지한다")
    void keepSingleVoteWhenFirstVotesAreConcurrent() throws Exception {
        PollContext context = savePollContext();
        Long optionId = context.poll().getOptions().getFirst().getId();
        given(currentUserProvider.getCurrentUserId()).willReturn(context.voter().getId());

        runConcurrently(CONCURRENT_REQUEST_COUNT, () ->
                pollVoteService.vote(context.poll().getPostId(), request(optionId))
        );

        List<PollVote> votes = pollVoteRepository.findAll();
        assertThat(votes).hasSize(1);
        assertThat(votes.getFirst().getOptionId()).isEqualTo(optionId);
    }

    private void runConcurrently(int requestCount, Runnable request) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < requestCount; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    request.run();
                    return null;
                }));
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private PollContext savePollContext() {
        User author = saveUser("author@example.com", "author");
        User voter = saveUser("voter@example.com", "voter");
        Post post = postRepository.saveAndFlush(new Post("투표 게시글", "본문", author));
        Poll poll = pollRepository.saveAndFlush(new Poll(post, List.of("Java", "Kotlin")));
        return new PollContext(author, voter, poll);
    }

    private User saveUser(String email, String nickname) {
        return userRepository.save(User.of(email, "encodedPassword", nickname, null));
    }

    private PollVoteRequestDto request(Long optionId) {
        PollVoteRequestDto request = new PollVoteRequestDto();
        ReflectionTestUtils.setField(request, "option_id", optionId);
        return request;
    }

    private record PollContext(User author, User voter, Poll poll) {
    }
}
