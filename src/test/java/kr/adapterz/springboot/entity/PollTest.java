package kr.adapterz.springboot.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PollTest {

    @Test
    @DisplayName("요청 순서대로 선택지를 생성하고 앞뒤 공백을 제거한다")
    void createPollOptionsInRequestOrder() {
        Poll poll = new Poll(createPost(), List.of("  Java  ", "Kotlin"));

        assertThat(poll.getOptions())
                .extracting(PollOption::getContent)
                .containsExactly("Java", "Kotlin");
        assertThat(poll.getOptions())
                .extracting(PollOption::getOptionOrder)
                .containsExactly(0, 1);
    }

    @Test
    @DisplayName("선택지는 2개 이상 5개 이하이어야 한다")
    void validatePollOptionCount() {
        assertThatThrownBy(() -> new Poll(createPost(), List.of("Java")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new Poll(createPost(), List.of("1", "2", "3", "4", "5")).getOptions())
                .hasSize(5);
        assertThatThrownBy(() -> new Poll(createPost(), List.of("1", "2", "3", "4", "5", "6")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("공백, null 또는 30자를 초과하는 선택지는 생성할 수 없다")
    void rejectInvalidPollOptionContent() {
        assertThatThrownBy(() -> new Poll(createPost(), List.of("   ", "Java")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Poll(createPost(), Arrays.asList(null, "Java")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Poll(createPost(), List.of("가".repeat(31), "Java")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("동일한 문구의 선택지를 허용한다")
    void allowDuplicatePollOptionContent() {
        Poll poll = new Poll(createPost(), List.of("Java", "Java"));

        assertThat(poll.getOptions())
                .extracting(PollOption::getContent)
                .containsExactly("Java", "Java");
    }

    @Test
    @DisplayName("외부에서 선택지 컬렉션을 변경할 수 없다")
    void preventExternalPollOptionModification() {
        Poll poll = new Poll(createPost(), List.of("Java", "Kotlin"));

        assertThatThrownBy(() -> poll.getOptions().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(poll.getOptions()).hasSize(2);
    }

    private Post createPost() {
        User author = User.of("test@example.com", "encodedPassword", "tester", null);
        return new Post("게시글 제목", "게시글 본문", author);
    }
}
