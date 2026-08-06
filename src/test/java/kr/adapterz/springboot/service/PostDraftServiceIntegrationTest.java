package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.PostDraftRequestDto;
import kr.adapterz.springboot.dto.PostDraftResponseDto;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.repository.PostDraftRepository;
import kr.adapterz.springboot.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class PostDraftServiceIntegrationTest {

    @Autowired
    private PostDraftService postDraftService;

    @Autowired
    private PostDraftRepository postDraftRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    private User user;

    @AfterEach
    void cleanUp() {
        postDraftRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("투표 선택지를 포함한 새 게시글 임시저장을 저장하고 조회한다")
    void saveAndGetDraftWithPollOptions() {
        user = saveUser();
        given(currentUserProvider.getCurrentUserId()).willReturn(user.getId());
        PostDraftRequestDto request = request(List.of("Java", "Kotlin"));

        PostDraftResponseDto saved = postDraftService.saveDraft(request);
        PostDraftResponseDto found = postDraftService.getDraft(null);

        assertThat(saved.getPollOptions()).containsExactly("Java", "Kotlin");
        assertThat(found.getPollOptions()).containsExactly("Java", "Kotlin");
    }

    @Test
    @DisplayName("임시저장을 갱신할 때 기존 투표 선택지도 함께 갱신한다")
    void updateDraftPollOptions() {
        user = saveUser();
        given(currentUserProvider.getCurrentUserId()).willReturn(user.getId());

        postDraftService.saveDraft(request(List.of("Java", "Kotlin")));
        postDraftService.saveDraft(request(List.of("Python")));

        assertThat(postDraftService.getDraft(null).getPollOptions())
                .containsExactly("Python");
    }

    private User saveUser() {
        return userRepository.save(User.of(
                "draft@example.com",
                "encodedPassword",
                "draft-user",
                null
        ));
    }

    private PostDraftRequestDto request(List<String> pollOptions) {
        PostDraftRequestDto request = new PostDraftRequestDto();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "title", "임시 게시글");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "content", "작성 중인 본문");
        org.springframework.test.util.ReflectionTestUtils.setField(request, "pollOptions", pollOptions);
        return request;
    }
}
