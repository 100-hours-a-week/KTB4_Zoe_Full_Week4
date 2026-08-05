package kr.adapterz.springboot.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PollResultOptionResponseDtoTest {

    @Test
    @DisplayName("득표율을 소수점 둘째 자리 문자열로 반올림한다")
    void calculateRoundedVoteRate() {
        PollResultOptionResponseDto response = new PollResultOptionResponseDto(1L, 1L, 3L);

        assertThat(response.getVoteRate()).isEqualTo("33.33");
    }

    @Test
    @DisplayName("정수 득표율도 소수점 둘째 자리까지 표시한다")
    void formatIntegerVoteRate() {
        PollResultOptionResponseDto response = new PollResultOptionResponseDto(1L, 1L, 2L);

        assertThat(response.getVoteRate()).isEqualTo("50.00");
    }

    @Test
    @DisplayName("전체 참여자가 없으면 득표율을 0.00으로 처리한다")
    void handleZeroTotalVoteCount() {
        PollResultOptionResponseDto response = new PollResultOptionResponseDto(1L, 0L, 0L);

        assertThat(response.getVoteRate()).isEqualTo("0.00");
    }
}
