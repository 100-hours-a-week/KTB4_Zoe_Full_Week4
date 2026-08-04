package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PollVoteCancelResponseDto {

    @JsonProperty("poll_id")
    private final Long pollId;

    @JsonProperty("has_voted")
    private final boolean hasVoted;

    @JsonProperty("total_vote_count")
    private final long totalVoteCount;

    public PollVoteCancelResponseDto(Long pollId, long totalVoteCount) {
        this.pollId = pollId;
        this.hasVoted = false;
        this.totalVoteCount = totalVoteCount;
    }
}
