package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.MultipartPostCreateRequestDto;
import kr.adapterz.springboot.dto.PostCreateResponseDto;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.repository.PollOptionRepository;
import kr.adapterz.springboot.repository.PollRepository;
import kr.adapterz.springboot.repository.PostRepository;
import kr.adapterz.springboot.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class PostCreationIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollOptionRepository pollOptionRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @AfterEach
    void cleanUp() {
        pollRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글과 투표 및 선택지를 하나의 생성 흐름으로 저장한다")
    void createPostWithPoll() {
        User author = saveAuthor();
        MultipartPostCreateRequestDto request = createRequest(List.of("  Java  ", "Kotlin"));
        given(currentUserProvider.getCurrentUserId()).willReturn(author.getId());
        given(imageStorageService.storePostImages(request.getImages())).willReturn(List.of());

        PostCreateResponseDto response = postService.createPost(request);

        assertThat(response.getPostId()).isNotNull();
        assertThat(response.getPollId()).isEqualTo(response.getPostId());
        assertThat(postRepository.count()).isOne();
        assertThat(pollRepository.count()).isOne();
        assertThat(pollOptionRepository.findAll())
                .extracting(option -> option.getContent(), option -> option.getOptionOrder())
                .containsExactlyInAnyOrder(
                        tuple("Java", 0),
                        tuple("Kotlin", 1)
                );
    }

    @Test
    @DisplayName("투표 생성에 실패하면 게시글도 롤백한다")
    void rollBackPostWhenPollCreationFails() {
        User author = saveAuthor();
        MultipartPostCreateRequestDto request = createRequest(List.of("Java"));
        given(currentUserProvider.getCurrentUserId()).willReturn(author.getId());
        given(imageStorageService.storePostImages(request.getImages())).willReturn(List.of());

        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(postRepository.count()).isZero();
        assertThat(pollRepository.count()).isZero();
        assertThat(pollOptionRepository.count()).isZero();
    }

    private User saveAuthor() {
        return userRepository.save(User.of(
                "author@example.com",
                "encodedPassword",
                "author",
                null
        ));
    }

    private MultipartPostCreateRequestDto createRequest(List<String> pollOptions) {
        MultipartPostCreateRequestDto request = new MultipartPostCreateRequestDto();
        request.setTitle("개발 언어 설문");
        request.setContent("선호하는 개발 언어를 선택해주세요.");
        request.setPoll_options(pollOptions);
        return request;
    }
}
