package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.PollOption;
import kr.adapterz.springboot.repository.PollVoteCountProjection;
import lombok.Getter;

import java.util.List;

@Getter
public class PollVoteResultResponseDto {

    @JsonProperty("total_vote_count")
    private final long totalVoteCount;

    private final List<PollResultOptionResponseDto> options;

    public PollVoteResultResponseDto(
            List<PollOption> pollOptions,
            List<PollVoteCountProjection> voteCounts,
            long totalVoteCount
    ) {
        this.totalVoteCount = totalVoteCount;
        this.options = new PollResultResponseDto(pollOptions, voteCounts, totalVoteCount).getOptions();
    }
}
