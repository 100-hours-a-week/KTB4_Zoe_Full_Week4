package kr.adapterz.springboot.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MultipartUserUpdateRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory()
            .getValidator();

    @Test
    @DisplayName("닉네임이 10글자를 초과하면 검증에 실패한다")
    void validateFailByLongNickname() {
        // given
        MultipartUserUpdateRequestDto request = new MultipartUserUpdateRequestDto();
        ReflectionTestUtils.setField(request, "nickname", "tooLongNick1");

        // when
        Set<ConstraintViolation<MultipartUserUpdateRequestDto>> violations = validator.validate(request);

        // then
        assertThat(violations)
                .anyMatch(violation -> violation.getMessage().equals("닉네임은 10글자 이하여야 합니다."));
    }

    @Test
    @DisplayName("닉네임에 띄어쓰기가 있으면 검증에 실패한다")
    void validateFailByBlankInNickname() {
        // given
        MultipartUserUpdateRequestDto request = new MultipartUserUpdateRequestDto();
        ReflectionTestUtils.setField(request, "nickname", "new nick");

        // when
        Set<ConstraintViolation<MultipartUserUpdateRequestDto>> violations = validator.validate(request);

        // then
        assertThat(violations)
                .anyMatch(violation -> violation.getMessage().equals("닉네임에는 띄어쓰기를 사용할 수 없습니다."));
    }

    @Test
    @DisplayName("닉네임이 비어 있으면 검증에 실패한다")
    void validateFailByBlankNickname() {
        // given
        MultipartUserUpdateRequestDto request = new MultipartUserUpdateRequestDto();
        ReflectionTestUtils.setField(request, "nickname", "");

        // when
        Set<ConstraintViolation<MultipartUserUpdateRequestDto>> violations = validator.validate(request);

        // then
        assertThat(violations)
                .anyMatch(violation -> violation.getMessage().equals("닉네임은 필수입니다."));
    }

    @Test
    @DisplayName("올바른 닉네임이면 검증에 성공한다")
    void validateSuccess() {
        // given
        MultipartUserUpdateRequestDto request = new MultipartUserUpdateRequestDto();
        ReflectionTestUtils.setField(request, "nickname", "newNick");

        // when
        Set<ConstraintViolation<MultipartUserUpdateRequestDto>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }
}