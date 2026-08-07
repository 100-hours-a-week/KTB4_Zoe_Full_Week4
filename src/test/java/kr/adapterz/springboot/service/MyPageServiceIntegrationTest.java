package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.MyPageActivitiesQuery;
import kr.adapterz.springboot.dto.MyPageActivityResponseDto;
import kr.adapterz.springboot.dto.MyPageQuery;
import kr.adapterz.springboot.dto.MyPageResponseDto;
import kr.adapterz.springboot.entity.Comment;
import kr.adapterz.springboot.entity.Like;
import kr.adapterz.springboot.entity.Poll;
import kr.adapterz.springboot.entity.PollVote;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.repository.CommentRepository;
import kr.adapterz.springboot.repository.LikeRepository;
import kr.adapterz.springboot.repository.PollRepository;
import kr.adapterz.springboot.repository.PollVoteRepository;
import kr.adapterz.springboot.repository.PostRepository;
import kr.adapterz.springboot.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
@Tag("mysql")
@Testcontainers
@TestPropertySource(properties = "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver")
class MyPageServiceIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Autowired
    private MyPageService myPageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollVoteRepository pollVoteRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    @DisplayName("작성글은 ACTIVE 게시글만 활동 시각과 post_id 내림차순으로 페이지네이션한다")
    void writtenActivitiesUseStableCursorPagination() {
        User user = saveUser("writer@example.com", "writer");
        Post oldest = savePost(user, "오래된 글", LocalDateTime.of(2026, 8, 1, 10, 0));
        Post middle = savePost(user, "중간 글", LocalDateTime.of(2026, 8, 2, 10, 0));
        Post newest = savePost(user, "최신 글", LocalDateTime.of(2026, 8, 3, 10, 0));
        Post blinded = savePost(user, "숨김 글", LocalDateTime.of(2026, 8, 4, 10, 0));
        blinded.blind();
        Post deleted = savePost(user, "삭제 글", LocalDateTime.of(2026, 8, 5, 10, 0));
        deleted.delete();
        given(currentUserProvider.getCurrentUserId()).willReturn(user.getId());

        MyPageQuery firstQuery = new MyPageQuery();
        firstQuery.setSize(2);
        MyPageResponseDto firstPage = myPageService.getMyPage(firstQuery);

        assertThat(firstPage.getActivity().getItems())
                .extracting(item -> item.getPostId())
                .containsExactly(newest.getId(), middle.getId());
        assertThat(firstPage.getActivity().isHasNext()).isTrue();
        assertThat(firstPage.getActivity().getItems())
                .noneMatch(item -> item.getPostId().equals(blinded.getId()));

        MyPageQuery secondQuery = new MyPageQuery();
        secondQuery.setSize(2);
        secondQuery.setCursor(firstPage.getActivity().getNextCursor());
        MyPageResponseDto secondPage = myPageService.getMyPage(secondQuery);

        assertThat(secondPage.getActivity().getItems())
                .extracting(item -> item.getPostId())
                .containsExactly(oldest.getId());
        assertThat(secondPage.getActivity().isHasNext()).isFalse();
    }

    @Test
    @DisplayName("참여 탭은 현재 투표만 사용하고 재투표 시 updated_at을 활동 시각으로 사용한다")
    void participatedActivitiesUseCurrentVotesAndUpdatedAt() {
        User author = saveUser("author@example.com", "author");
        User user = saveUser("voter@example.com", "voter");
        Post firstPost = savePost(author, "첫 투표", LocalDateTime.of(2026, 8, 1, 10, 0));
        Post secondPost = savePost(author, "두 번째 투표", LocalDateTime.of(2026, 8, 2, 10, 0));
        Poll firstPoll = savePoll(firstPost);
        Poll secondPoll = savePoll(secondPost);
        PollVote firstVote = saveVote(firstPoll, user);
        PollVote secondVote = saveVote(secondPoll, user);
        ReflectionTestUtils.setField(firstVote, "updatedAt", LocalDateTime.of(2026, 8, 4, 10, 0));
        ReflectionTestUtils.setField(secondVote, "updatedAt", LocalDateTime.of(2026, 8, 3, 10, 0));
        pollVoteRepository.flush();
        given(currentUserProvider.getCurrentUserId()).willReturn(user.getId());

        MyPageActivitiesQuery query = new MyPageActivitiesQuery();
        query.setTab("participated");
        MyPageActivityResponseDto response = myPageService.getActivities(query);

        assertThat(response.getItems())
                .extracting(item -> item.getPostId())
                .containsExactly(firstPost.getId(), secondPost.getId());
        assertThat(response.getItems().getFirst().getActivityAt())
                .isEqualTo(LocalDateTime.of(2026, 8, 4, 10, 0));
        assertThat(response.getItems())
                .allMatch(item -> item.isVoted());
    }

    @Test
    @DisplayName("통계와 활동 항목은 현재 좋아요·댓글·투표 수를 반환한다")
    void returnsStatsAndActivityCounts() {
        User user = saveUser("owner@example.com", "owner");
        User voter = saveUser("voter@example.com", "voter");
        Post post = savePost(user, "활동 글", LocalDateTime.of(2026, 8, 1, 10, 0));
        Poll poll = savePoll(post);
        saveVote(poll, voter);
        saveVote(poll, user);
        likeRepository.saveAndFlush(new Like(post, voter));
        commentRepository.saveAndFlush(new Comment("댓글", post, voter));
        given(currentUserProvider.getCurrentUserId()).willReturn(user.getId());

        MyPageQuery query = new MyPageQuery();
        MyPageResponseDto response = myPageService.getMyPage(query);

        assertThat(response.getStats().getPostCount()).isOne();
        assertThat(response.getStats().getPollParticipationCount()).isOne();
        assertThat(response.getStats().getReceivedLikeCount()).isOne();
        assertThat(response.getActivity().getItems().getFirst().getLikeCount()).isOne();
        assertThat(response.getActivity().getItems().getFirst().getCommentCount()).isOne();
        assertThat(response.getActivity().getItems().getFirst().getParticipantCount()).isEqualTo(2);
        assertThat(response.getActivity().getItems().getFirst().isVoted()).isTrue();
    }

    @Test
    @DisplayName("현재 투표를 취소하면 참여 목록과 참여 통계에서 제외한다")
    void cancelVoteRemovesParticipation() {
        User author = saveUser("cancel-author@example.com", "cancel-author");
        User user = saveUser("cancel-voter@example.com", "cancel-voter");
        Post post = savePost(author, "취소할 투표", LocalDateTime.of(2026, 8, 1, 10, 0));
        Poll poll = savePoll(post);
        PollVote vote = saveVote(poll, user);
        pollVoteRepository.flush();
        given(currentUserProvider.getCurrentUserId()).willReturn(user.getId());

        MyPageActivitiesQuery activitiesQuery = new MyPageActivitiesQuery();
        activitiesQuery.setTab("participated");
        assertThat(myPageService.getActivities(activitiesQuery).getItems()).hasSize(1);

        pollVoteRepository.deleteCurrentVote(poll.getPostId(), user.getId());
        pollVoteRepository.flush();

        MyPageQuery myPageQuery = new MyPageQuery();
        MyPageResponseDto response = myPageService.getMyPage(myPageQuery);

        assertThat(response.getStats().getPollParticipationCount()).isZero();
        assertThat(myPageService.getActivities(activitiesQuery).getItems()).isEmpty();
    }

    private User saveUser(String email, String nickname) {
        return userRepository.saveAndFlush(User.of(email, "encodedPassword", nickname, null));
    }

    private Post savePost(User author, String title, LocalDateTime createdAt) {
        Post post = postRepository.saveAndFlush(new Post(title, "본문", author));
        ReflectionTestUtils.setField(post, "createdAt", createdAt);
        return postRepository.saveAndFlush(post);
    }

    private Poll savePoll(Post post) {
        return pollRepository.saveAndFlush(new Poll(post, List.of("Java", "Kotlin")));
    }

    private PollVote saveVote(Poll poll, User user) {
        PollVote vote = new PollVote(poll, user, poll.getOptions().getFirst());
        pollVoteRepository.persist(vote);
        return vote;
    }
}
