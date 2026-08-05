package kr.adapterz.springboot.repository;

import jakarta.persistence.EntityManager;
import kr.adapterz.springboot.entity.Poll;
import kr.adapterz.springboot.entity.PollOption;
import kr.adapterz.springboot.entity.PollVote;
import kr.adapterz.springboot.entity.PollVoteId;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PollVoteRepositoryTest {

    @Autowired
    private PollVoteRepository pollVoteRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("사용자의 현재 선택을 저장한다")
    void savePollVote() {
        User user = saveUser("voter@example.com", "voter");
        Poll poll = savePoll(user, "첫 번째 게시글");
        PollOption option = poll.getOptions().get(0);

        pollVoteRepository.persist(new PollVote(poll, user, option));
        entityManager.flush();

        assertThat(pollVoteRepository.existsById(new PollVoteId(poll.getPostId(), user.getId())))
                .isTrue();
    }

    @Test
    @DisplayName("한 사용자는 서로 다른 투표에 각각 참여할 수 있다")
    void saveVotesForDifferentPolls() {
        User user = saveUser("voter@example.com", "voter");
        Poll firstPoll = savePoll(user, "첫 번째 게시글");
        Poll secondPoll = savePoll(user, "두 번째 게시글");

        pollVoteRepository.persist(new PollVote(firstPoll, user, firstPoll.getOptions().get(0)));
        pollVoteRepository.persist(new PollVote(secondPoll, user, secondPoll.getOptions().get(1)));
        entityManager.flush();

        assertThat(pollVoteRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("여러 사용자는 같은 투표에 참여할 수 있다")
    void saveVotesFromDifferentUsers() {
        User author = saveUser("author@example.com", "author");
        User firstVoter = saveUser("first@example.com", "first");
        User secondVoter = saveUser("second@example.com", "second");
        Poll poll = savePoll(author, "게시글");

        pollVoteRepository.persist(new PollVote(poll, firstVoter, poll.getOptions().get(0)));
        pollVoteRepository.persist(new PollVote(poll, secondVoter, poll.getOptions().get(1)));
        entityManager.flush();

        assertThat(pollVoteRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("같은 사용자는 같은 투표에서 두 개의 현재 선택을 저장할 수 없다")
    void rejectDuplicateCurrentVote() {
        User user = saveUser("voter@example.com", "voter");
        Poll poll = savePoll(user, "게시글");
        pollVoteRepository.persist(new PollVote(poll, user, poll.getOptions().get(0)));
        entityManager.flush();
        entityManager.clear();

        Poll managedPoll = pollRepository.findById(poll.getPostId()).orElseThrow();
        User managedUser = userRepository.findById(user.getId()).orElseThrow();

        assertThatThrownBy(() -> {
            entityManager.persist(new PollVote(managedPoll, managedUser, managedPoll.getOptions().get(1)));
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("다른 투표에 속한 선택지는 현재 선택으로 저장할 수 없다")
    void rejectOptionFromDifferentPoll() {
        User user = saveUser("voter@example.com", "voter");
        Poll selectedPoll = savePoll(user, "첫 번째 게시글");
        Poll otherPoll = savePoll(user, "두 번째 게시글");

        assertThatThrownBy(() -> new PollVote(
                selectedPoll,
                user,
                otherPoll.getOptions().get(0)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("선택지가 해당 투표에 속하지 않습니다.");
    }

    @Test
    @DisplayName("투표를 물리 삭제하면 현재 선택도 연쇄 삭제한다")
    void cascadeDeleteVoteWithPoll() {
        User user = saveUser("voter@example.com", "voter");
        Poll poll = savePoll(user, "게시글");
        pollVoteRepository.persist(new PollVote(poll, user, poll.getOptions().get(0)));
        entityManager.flush();
        Long postId = poll.getPostId();
        entityManager.clear();

        postRepository.deleteById(postId);
        postRepository.flush();

        assertThat(pollVoteRepository.count()).isZero();
    }

    @Test
    @DisplayName("사용자를 소프트 삭제해도 현재 선택은 유지한다")
    void keepVoteAfterSoftDeletingUser() {
        User user = saveUser("voter@example.com", "voter");
        Poll poll = savePoll(user, "게시글");
        pollVoteRepository.persist(new PollVote(poll, user, poll.getOptions().get(0)));
        entityManager.flush();

        user.delete();
        userRepository.saveAndFlush(user);

        assertThat(pollVoteRepository.count()).isOne();
    }

    private User saveUser(String email, String nickname) {
        return userRepository.save(User.of(email, "encodedPassword", nickname, null));
    }

    private Poll savePoll(User author, String title) {
        Post post = postRepository.saveAndFlush(new Post(title, "본문", author));
        return pollRepository.saveAndFlush(new Poll(post, List.of("Java", "Kotlin")));
    }
}
