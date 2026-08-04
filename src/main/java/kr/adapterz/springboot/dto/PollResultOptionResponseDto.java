package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class PollResultOptionResponseDto {

    @JsonProperty("option_id")
    private final Long optionId;

    @JsonProperty("vote_count")
    private final long voteCount;

    @JsonProperty("vote_rate")
    private final String voteRate;

    public PollResultOptionResponseDto(Long optionId, long voteCount, long totalVoteCount) {
        this.optionId = optionId;
        this.voteCount = voteCount;
        this.voteRate = calculateVoteRate(voteCount, totalVoteCount);
    }

    private String calculateVoteRate(long voteCount, long totalVoteCount) {
        if (totalVoteCount == 0) {
            return "0.00";
        }

        return BigDecimal.valueOf(voteCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalVoteCount), 2, RoundingMode.HALF_UP)
                .toPlainString();
    }
}
