package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.auth.UnauthorizedException;
import kr.adapterz.springboot.dto.PollResponseDto;
import kr.adapterz.springboot.dto.PostListResponseDto;
import kr.adapterz.springboot.dto.PostSummaryResponseDto;
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
class PostListPollIntegrationTest {

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
    @DisplayName("게시글이 없으면 빈 목록을 반환한다")
    void getEmptyPostList() {
        PostListResponseDto response = postService.getPosts(null, 20);

        assertThat(response.getPosts()).isEmpty();
        assertThat(response.getNextCursor()).isNull();
        assertThat(response.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("비로그인 목록에는 투표 기본 정보와 전체 참여 인원만 제공한다")
    void getPollsWithoutResultsAsGuest() throws Exception {
        User author = saveUser("author@example.com", "author");
        User voter = saveUser("voter@example.com", "voter");
        Post post = savePostWithPoll(author, "투표 게시글");
        Poll poll = pollRepository.findByPostIdWithOptions(post.getId()).orElseThrow();
        saveVote(poll, voter, 0);
        given(currentUserProvider.getCurrentUserId()).willThrow(new UnauthorizedException());

        PostListResponseDto response = postService.getPosts(null, 20);
        PollResponseDto pollResponse = findPost(response, post.getId()).getPoll();
        String pollJson = objectMapper.writeValueAsString(pollResponse);

        assertThat(pollResponse.isHasVoted()).isFalse();
        assertThat(pollResponse.getTotalVoteCount()).isOne();
        assertThat(pollResponse.getOptions())
                .extracting(option -> option.getContent())
                .containsExactly("Java", "Kotlin");
        assertThat(pollJson)
                .doesNotContain("selected_option_id")
                .doesNotContain("result");
    }

    @Test
    @DisplayName("목록의 각 투표마다 현재 사용자의 참여 여부를 독립적으로 판단한다")
    void exposeResultsOnlyForPollsVotedByCurrentUser() {
        User author = saveUser("author@example.com", "author");
        User currentUser = saveUser("current@example.com", "current");
        User otherUser = saveUser("other@example.com", "other");
        Post votedPost = savePostWithPoll(author, "참여한 투표");
        Post unvotedPost = savePostWithPoll(author, "참여하지 않은 투표");
        Post legacyPost = postRepository.saveAndFlush(new Post("레거시 게시글", "본문", author));
        Poll votedPoll = pollRepository.findByPostIdWithOptions(votedPost.getId()).orElseThrow();
        Poll unvotedPoll = pollRepository.findByPostIdWithOptions(unvotedPost.getId()).orElseThrow();
        saveVote(votedPoll, currentUser, 0);
        saveVote(votedPoll, otherUser, 1);
        saveVote(unvotedPoll, otherUser, 1);
        given(currentUserProvider.getCurrentUserId()).willReturn(currentUser.getId());

        PostListResponseDto response = postService.getPosts(null, 20);
        PollResponseDto votedResponse = findPost(response, votedPost.getId()).getPoll();
        PollResponseDto unvotedResponse = findPost(response, unvotedPost.getId()).getPoll();

        assertThat(votedResponse.isHasVoted()).isTrue();
        assertThat(votedResponse.getSelectedOptionId()).isEqualTo(votedPoll.getOptions().get(0).getId());
        assertThat(votedResponse.getTotalVoteCount()).isEqualTo(2);
        assertThat(votedResponse.getResult().getOptions())
                .extracting(option -> option.getVoteRate())
                .containsExactly("50.00", "50.00");

        assertThat(unvotedResponse.isHasVoted()).isFalse();
        assertThat(unvotedResponse.getTotalVoteCount()).isOne();
        assertThat(unvotedResponse.getResult()).isNull();
        assertThat(findPost(response, legacyPost.getId()).getPoll()).isNull();
    }

    @Test
    @DisplayName("투표 정보 추가 후에도 게시글 커서 페이지네이션을 유지한다")
    void keepCursorPaginationWithPolls() {
        User author = saveUser("author@example.com", "author");
        Post firstPost = savePostWithPoll(author, "첫 번째 게시글");
        Post secondPost = savePostWithPoll(author, "두 번째 게시글");
        Post thirdPost = savePostWithPoll(author, "세 번째 게시글");
        given(currentUserProvider.getCurrentUserId()).willThrow(new UnauthorizedException());

        PostListResponseDto firstPage = postService.getPosts(null, 2);
        PostListResponseDto secondPage = postService.getPosts(firstPage.getNextCursor(), 2);

        assertThat(firstPage.getPosts())
                .extracting(PostSummaryResponseDto::getPostId)
                .containsExactly(thirdPost.getId(), secondPost.getId());
        assertThat(firstPage.isHasNext()).isTrue();
        assertThat(firstPage.getNextCursor()).isEqualTo(secondPost.getId());
        assertThat(secondPage.getPosts())
                .extracting(PostSummaryResponseDto::getPostId)
                .containsExactly(firstPost.getId());
        assertThat(secondPage.isHasNext()).isFalse();
    }

    private PostSummaryResponseDto findPost(PostListResponseDto response, Long postId) {
        return response.getPosts().stream()
                .filter(post -> post.getPostId().equals(postId))
                .findFirst()
                .orElseThrow();
    }

    private User saveUser(String email, String nickname) {
        return userRepository.save(User.of(email, "encodedPassword", nickname, null));
    }

    private Post savePostWithPoll(User author, String title) {
        Post post = postRepository.saveAndFlush(new Post(title, "본문", author));
        pollRepository.saveAndFlush(new Poll(post, List.of("Java", "Kotlin")));
        return post;
    }

    private void saveVote(Poll poll, User user, int optionIndex) {
        pollVoteRepository.persist(new PollVote(poll, user, poll.getOptions().get(optionIndex)));
    }
}
