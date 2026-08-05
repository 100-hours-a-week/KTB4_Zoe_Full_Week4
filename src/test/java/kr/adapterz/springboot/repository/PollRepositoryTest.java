package kr.adapterz.springboot.repository;

import jakarta.persistence.EntityManager;
import kr.adapterz.springboot.entity.Poll;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.entity.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PollRepositoryTest {

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollOptionRepository pollOptionRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("게시글과 공유 기본키를 사용하는 투표와 선택지를 저장한다")
    void savePollWithOptions() {
        Post post = savePost();

        Poll savedPoll = pollRepository.saveAndFlush(new Poll(post, List.of("Java", "Kotlin")));

        assertThat(savedPoll.getPostId()).isEqualTo(post.getId());
        assertThat(savedPoll.getOptions())
                .extracting(option -> option.getContent())
                .containsExactly("Java", "Kotlin");
    }

    @Test
    @DisplayName("한 게시글에 두 번째 투표를 저장할 수 없다")
    void rejectSecondPollForSamePost() {
        Post post = savePost();
        pollRepository.saveAndFlush(new Poll(post, List.of("Java", "Kotlin")));

        assertThatThrownBy(() -> pollRepository.saveAndFlush(new Poll(post, List.of("Python", "Go"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("게시글을 물리 삭제하면 투표와 선택지도 연쇄 삭제한다")
    void cascadeDeletePollAndOptionsWithPost() {
        Post post = savePost();
        Poll poll = pollRepository.saveAndFlush(new Poll(post, List.of("Java", "Kotlin")));
        Long pollId = poll.getPostId();
        entityManager.clear();

        postRepository.deleteById(post.getId());
        postRepository.flush();

        assertThat(pollRepository.existsById(pollId)).isFalse();
        assertThat(pollOptionRepository.count()).isZero();
    }

    private Post savePost() {
        User author = userRepository.save(User.of(
                "test@example.com",
                "encodedPassword",
                "tester",
                null
        ));
        return postRepository.saveAndFlush(new Post("게시글 제목", "게시글 본문", author));
    }
}
