package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Poll;
import kr.adapterz.springboot.repository.PollVoteCountProjection;
import lombok.Getter;

import java.util.List;

@Getter
public class PollVoteUpdateResponseDto {

    @JsonProperty("poll_id")
    private final Long pollId;

    @JsonProperty("selected_option_id")
    private final Long selectedOptionId;

    private final PollVoteResultResponseDto result;

    public PollVoteUpdateResponseDto(
            Poll poll,
            Long selectedOptionId,
            long totalVoteCount,
            List<PollVoteCountProjection> voteCounts
    ) {
        this.pollId = poll.getPostId();
        this.selectedOptionId = selectedOptionId;
        this.result = new PollVoteResultResponseDto(poll.getOptions(), voteCounts, totalVoteCount);
    }
}
