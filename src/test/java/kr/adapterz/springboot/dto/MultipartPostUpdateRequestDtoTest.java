package kr.adapterz.springboot.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultipartPostUpdateRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("게시글 수정 요청은 투표 선택지 없이 검증에 성공한다")
    void validateSuccessWithoutPollOptions() {
        MultipartPostUpdateRequestDto request = new MultipartPostUpdateRequestDto();
        request.setTitle("수정된 게시글 제목");
        request.setContent("수정된 게시글 본문");

        assertThat(validator.validate(request)).isEmpty();
    }
}
