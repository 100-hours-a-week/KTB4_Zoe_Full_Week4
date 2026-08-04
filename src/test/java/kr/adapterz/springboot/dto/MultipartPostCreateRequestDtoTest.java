package kr.adapterz.springboot.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MultipartPostCreateRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("선택지가 누락되면 검증에 실패한다")
    void validateFailByMissingPollOptions() {
        MultipartPostCreateRequestDto request = validRequest();
        request.setPoll_options(null);

        Set<ConstraintViolation<MultipartPostCreateRequestDto>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(violation -> violation.getMessage().equals("선택지는 필수입니다."));
    }

    @Test
    @DisplayName("선택지가 1개이면 검증에 실패한다")
    void validateFailByOnePollOption() {
        MultipartPostCreateRequestDto request = validRequest();
        request.setPoll_options(List.of("Java"));

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getMessage().equals("선택지는 2개 이상 5개 이하여야 합니다."));
    }

    @Test
    @DisplayName("선택지가 2개 또는 5개이면 검증에 성공한다")
    void validateSuccessAtPollOptionCountBoundaries() {
        MultipartPostCreateRequestDto minimumRequest = validRequest();
        MultipartPostCreateRequestDto maximumRequest = validRequest();
        maximumRequest.setPoll_options(List.of("1", "2", "3", "4", "5"));

        assertThat(validator.validate(minimumRequest)).isEmpty();
        assertThat(validator.validate(maximumRequest)).isEmpty();
    }

    @Test
    @DisplayName("선택지가 6개이면 검증에 실패한다")
    void validateFailBySixPollOptions() {
        MultipartPostCreateRequestDto request = validRequest();
        request.setPoll_options(List.of("1", "2", "3", "4", "5", "6"));

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getMessage().equals("선택지는 2개 이상 5개 이하여야 합니다."));
    }

    @Test
    @DisplayName("빈 값, 공백 값 또는 null 선택지가 있으면 검증에 실패한다")
    void validateFailByBlankOrNullPollOption() {
        List<List<String>> invalidOptions = List.of(
                List.of("", "Java"),
                List.of("   ", "Java"),
                Arrays.asList(null, "Java")
        );

        for (List<String> options : invalidOptions) {
            MultipartPostCreateRequestDto request = validRequest();
            request.setPoll_options(options);

            assertThat(validator.validate(request))
                    .anyMatch(violation -> violation.getMessage()
                            .equals("각 선택지는 앞뒤 공백 제거 후 1자 이상 30자 이하여야 합니다."));
        }
    }

    @Test
    @DisplayName("선택지는 앞뒤 공백을 제거한 길이를 기준으로 검증한다")
    void validatePollOptionByNormalizedLength() {
        MultipartPostCreateRequestDto validRequest = validRequest();
        validRequest.setPoll_options(List.of("  " + "가".repeat(30) + "  ", "Java"));
        MultipartPostCreateRequestDto invalidRequest = validRequest();
        invalidRequest.setPoll_options(List.of("  " + "가".repeat(31) + "  ", "Java"));

        assertThat(validator.validate(validRequest)).isEmpty();
        assertThat(validator.validate(invalidRequest))
                .anyMatch(violation -> violation.getMessage()
                        .equals("각 선택지는 앞뒤 공백 제거 후 1자 이상 30자 이하여야 합니다."));
    }

    @Test
    @DisplayName("같은 문구의 선택지는 허용한다")
    void validateSuccessWithDuplicatePollOptions() {
        MultipartPostCreateRequestDto request = validRequest();
        request.setPoll_options(List.of("Java", "Java"));

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("poll_options 별칭으로 받은 선택지를 camelCase getter로 제공한다")
    void bindPollOptionsAliasAndExposeThroughCamelCaseGetter() {
        MultipartPostCreateRequestDto request = new MultipartPostCreateRequestDto();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addParameter("poll_options", "Java", "Kotlin");

        ServletRequestDataBinder binder = new ServletRequestDataBinder(request);
        binder.bind(servletRequest);

        assertThat(request.getPollOptions()).containsExactly("Java", "Kotlin");
    }

    private MultipartPostCreateRequestDto validRequest() {
        MultipartPostCreateRequestDto request = new MultipartPostCreateRequestDto();
        request.setTitle("개발 언어 설문");
        request.setContent("선호하는 개발 언어를 선택해주세요.");
        request.setPoll_options(List.of("Java", "Kotlin"));
        return request;
    }
}
