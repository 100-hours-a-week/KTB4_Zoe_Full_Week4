package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.MultipartPostUpdateRequestDto;
import kr.adapterz.springboot.dto.PollVoteRequestDto;
import kr.adapterz.springboot.dto.PollVoteUpdateResponseDto;
import kr.adapterz.springboot.dto.PollUpdateRequestDto;
import kr.adapterz.springboot.dto.PostUpdateResponseDto;
import kr.adapterz.springboot.entity.Poll;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.PollOptionsLockedException;
import kr.adapterz.springboot.repository.PollOptionRepository;
import kr.adapterz.springboot.repository.PollRepository;
import kr.adapterz.springboot.repository.PollVoteRepository;
import kr.adapterz.springboot.repository.PostRepository;
import kr.adapterz.springboot.repository.PostVersionRepository;
import kr.adapterz.springboot.repository.UserRepository;
import kr.adapterz.springboot.service.PollVoteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@Tag("mysql")
@Testcontainers
@SpringBootTest
@TestPropertySource(properties = "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver")
class PostUpdatePollIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Autowired
    private PostService postService;

    @Autowired
    private PollVoteService pollVoteService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollOptionRepository pollOptionRepository;

    @Autowired
    private PollVoteRepository pollVoteRepository;

    @Autowired
    private PostVersionRepository postVersionRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @AfterEach
    void cleanDatabase() {
        pollVoteRepository.deleteAll();
        pollRepository.deleteAll();
        postVersionRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("poll JSON 파트가 있으면 선택지를 유지·추가·재정렬한다")
    void updatePostAndPollTogether() {
        PollContext context = saveContext();
        given(currentUserProvider.getCurrentUserId()).willReturn(context.author().getId());
        given(imageStorageService.storePostImages(org.mockito.ArgumentMatchers.any())).willReturn(List.of());

        PollUpdateRequestDto pollRequest = pollRequest(List.of(
                option(context.poll().getOptions().get(1).getId(), " Python "),
                option(null, "Go")
        ));

        PostUpdateResponseDto response = postService.updatePost(
                context.post().getId(),
                postRequest("수정 제목", "수정 본문"),
                pollRequest
        );

        Poll updatedPoll = pollRepository.findByPostIdWithOptions(context.post().getId()).orElseThrow();
        assertThat(response.getPoll().getOptions())
                .extracting(option -> option.getContent())
                .containsExactly("Python", "Go");
        assertThat(updatedPoll.getOptions())
                .extracting(option -> option.getContent(), option -> option.getOptionOrder())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Python", 0),
                        org.assertj.core.groups.Tuple.tuple("Go", 1)
                );
        assertThat(postRepository.findById(context.post().getId()).orElseThrow().getTitle())
                .isEqualTo("수정 제목");
    }

    @Test
    @DisplayName("poll 파트가 없으면 투표 잠금 확인 없이 게시글만 수정한다")
    void updatePostWithoutPoll() {
        PollContext context = saveContext();
        given(currentUserProvider.getCurrentUserId()).willReturn(context.author().getId());
        given(imageStorageService.storePostImages(org.mockito.ArgumentMatchers.any())).willReturn(List.of());
        voteForTest(context);

        postService.updatePost(
                context.post().getId(),
                postRequest("게시글만 수정", "본문"),
                null
        );

        assertThat(postRepository.findById(context.post().getId()).orElseThrow().getTitle())
                .isEqualTo("게시글만 수정");
        then(imageStorageService).should().storePostImages(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("현재 참여자가 있으면 게시글과 투표를 모두 수정하지 않는다")
    void rejectPollUpdateWhenVoted() {
        PollContext context = saveContext();
        given(currentUserProvider.getCurrentUserId()).willReturn(context.author().getId());
        given(imageStorageService.storePostImages(org.mockito.ArgumentMatchers.any())).willReturn(List.of());
        voteForTest(context);

        assertThatThrownBy(() -> postService.updatePost(
                context.post().getId(),
                postRequest("실패 제목", "실패 본문"),
                pollRequest(List.of(option(context.poll().getOptions().getFirst().getId(), "Java"), option(null, "Go")))
        )).isInstanceOf(PollOptionsLockedException.class);

        assertThat(postRepository.findById(context.post().getId()).orElseThrow().getTitle())
                .isEqualTo("원래 제목");
        then(imageStorageService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("다른 사용자의 동시 참여와 투표 포함 게시글 수정은 한 요청씩 처리한다")
    void serializeConcurrentVoteAndPollUpdate() throws Exception {
        PollContext context = saveContext();
        User voter = userRepository.saveAndFlush(User.of("update-voter@example.com", "password", "update-voter", null));
        given(imageStorageService.storePostImages(org.mockito.ArgumentMatchers.any())).willReturn(List.of());

        ThreadLocal<Long> userIdByThread = new ThreadLocal<>();
        given(currentUserProvider.getCurrentUserId()).willAnswer(invocation -> userIdByThread.get());
        PollVoteRequestDto voteRequest = new PollVoteRequestDto();
        ReflectionTestUtils.setField(voteRequest, "option_id", context.poll().getOptions().getFirst().getId());
        PollUpdateRequestDto updateRequest = pollRequest(List.of(
                option(context.poll().getOptions().getFirst().getId(), "Java"),
                option(null, "Python")
        ));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        Object updateResult = null;
        try {
            Future<Object> update = executor.submit(() -> {
                userIdByThread.set(context.author().getId());
                try {
                    ready.countDown();
                    start.await();
                    return postService.updatePost(context.post().getId(), postRequest("동시 수정", "본문"), updateRequest);
                } catch (RuntimeException e) {
                    return e;
                } finally {
                    userIdByThread.remove();
                }
            });
            Future<Object> vote = executor.submit(() -> {
                userIdByThread.set(voter.getId());
                try {
                    ready.countDown();
                    start.await();
                    return pollVoteService.vote(context.post().getId(), voteRequest);
                } catch (RuntimeException e) {
                    return e;
                } finally {
                    userIdByThread.remove();
                }
            });

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            updateResult = update.get(10, TimeUnit.SECONDS);
            Object voteResult = vote.get(10, TimeUnit.SECONDS);

            assertThat(updateResult)
                    .isInstanceOfAny(PostUpdateResponseDto.class, PollOptionsLockedException.class);
            assertThat(voteResult).isInstanceOf(PollVoteUpdateResponseDto.class);
        } finally {
            executor.shutdownNow();
        }

        assertThat(pollVoteRepository.count()).isOne();
        assertThat(updateResult).isNotNull();
        Poll updatedPoll = pollRepository.findByPostIdWithOptions(context.post().getId()).orElseThrow();
        Post updatedPost = postRepository.findById(context.post().getId()).orElseThrow();

        if (updateResult instanceof PostUpdateResponseDto) {
            assertThat(updatedPost.getTitle()).isEqualTo("동시 수정");
            assertThat(updatedPoll.getOptions())
                    .extracting(option -> option.getContent())
                    .containsExactly("Java", "Python");
        } else {
            assertThat(updateResult).isInstanceOf(PollOptionsLockedException.class);
            assertThat(updatedPost.getTitle()).isEqualTo("원래 제목");
            assertThat(updatedPoll.getOptions())
                    .extracting(option -> option.getContent())
                    .containsExactly("Java", "Kotlin");
        }
    }

    @Test
    @DisplayName("다른 투표의 option_id가 포함되면 게시글과 투표를 수정하지 않는다")
    void rejectOptionIdFromAnotherPoll() {
        PollContext context = saveContext();
        PollContext other = saveContext("other-update-author@example.com", "other-update-author");
        given(currentUserProvider.getCurrentUserId()).willReturn(context.author().getId());
        given(imageStorageService.storePostImages(org.mockito.ArgumentMatchers.any())).willReturn(List.of());

        assertThatThrownBy(() -> postService.updatePost(
                context.post().getId(),
                postRequest("실패 제목", "실패 본문"),
                pollRequest(List.of(
                        option(other.poll().getOptions().getFirst().getId(), "Java"),
                        option(null, "Python")
                ))
        )).isInstanceOf(kr.adapterz.springboot.exception.PollOptionUpdateInvalidException.class);

        assertThat(postRepository.findById(context.post().getId()).orElseThrow().getTitle())
                .isEqualTo("원래 제목");
        assertThat(pollRepository.findByPostIdWithOptions(context.post().getId()).orElseThrow().getOptions())
                .extracting(option -> option.getContent())
                .containsExactly("Java", "Kotlin");
    }

    @Test
    @DisplayName("선택지는 최대 5개까지 수정할 수 있다")
    void updateFiveOptions() {
        PollContext context = saveContext();
        given(currentUserProvider.getCurrentUserId()).willReturn(context.author().getId());
        given(imageStorageService.storePostImages(org.mockito.ArgumentMatchers.any())).willReturn(List.of());

        postService.updatePost(
                context.post().getId(),
                postRequest("다섯 선택지", "본문"),
                pollRequest(List.of(
                        option(context.poll().getOptions().get(0).getId(), "Java"),
                        option(context.poll().getOptions().get(1).getId(), "Kotlin"),
                        option(null, "Python"),
                        option(null, "Go"),
                        option(null, "Rust")
                ))
        );

        assertThat(pollRepository.findByPostIdWithOptions(context.post().getId()).orElseThrow().getOptions())
                .hasSize(5)
                .extracting(option -> option.getOptionOrder())
                .containsExactly(0, 1, 2, 3, 4);
    }

    private PollContext saveContext() {
        return saveContext("update-author@example.com", "update-author");
    }

    private PollContext saveContext(String email, String nickname) {
        User author = userRepository.saveAndFlush(User.of(email, "password", nickname, null));
        Post post = postRepository.saveAndFlush(new Post("원래 제목", "원래 본문", author));
        Poll poll = pollRepository.saveAndFlush(new Poll(post, List.of("Java", "Kotlin")));
        return new PollContext(author, post, poll);
    }

    private void voteForTest(PollContext context) {
        pollVoteRepository.saveAndFlush(new kr.adapterz.springboot.entity.PollVote(
                context.poll(),
                context.author(),
                context.poll().getOptions().getFirst()
        ));
    }

    private MultipartPostUpdateRequestDto postRequest(String title, String content) {
        MultipartPostUpdateRequestDto request = new MultipartPostUpdateRequestDto();
        request.setTitle(title);
        request.setContent(content);
        return request;
    }

    private PollUpdateRequestDto pollRequest(List<PollUpdateRequestDto.Option> options) {
        PollUpdateRequestDto request = new PollUpdateRequestDto();
        ReflectionTestUtils.setField(request, "options", options);
        return request;
    }

    private PollUpdateRequestDto.Option option(Long optionId, String content) {
        PollUpdateRequestDto.Option option = new PollUpdateRequestDto.Option();
        ReflectionTestUtils.setField(option, "optionId", optionId);
        ReflectionTestUtils.setField(option, "content", content);
        return option;
    }

    private record PollContext(User author, Post post, Poll poll) {
    }
}
