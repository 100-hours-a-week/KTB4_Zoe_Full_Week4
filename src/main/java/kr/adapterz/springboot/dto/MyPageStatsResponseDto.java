package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class MyPageStatsResponseDto {

    @JsonProperty("post_count")
    private final long postCount;

    @JsonProperty("poll_participation_count")
    private final long pollParticipationCount;

    @JsonProperty("received_like_count")
    private final long receivedLikeCount;

    public MyPageStatsResponseDto(
            long postCount,
            long pollParticipationCount,
            long receivedLikeCount
    ) {
        this.postCount = postCount;
        this.pollParticipationCount = pollParticipationCount;
        this.receivedLikeCount = receivedLikeCount;
    }
}
