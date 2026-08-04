package kr.adapterz.springboot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.entity.Poll;
import kr.adapterz.springboot.entity.PollVote;
import kr.adapterz.springboot.repository.PollVoteCountProjection;
import lombok.Getter;

import java.util.List;

@Getter
public class PollResponseDto {

    @JsonProperty("poll_id")
    private final Long pollId;

    private final List<PollOptionResponseDto> options;

    @JsonProperty("has_voted")
    private final boolean hasVoted;

    @JsonProperty("total_vote_count")
    private final long totalVoteCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("selected_option_id")
    private final Long selectedOptionId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final PollResultResponseDto result;

    private PollResponseDto(
            Poll poll,
            long totalVoteCount,
            PollVote currentVote,
            List<PollVoteCountProjection> voteCounts
    ) {
        this.pollId = poll.getPostId();
        this.options = poll.getOptions().stream()
                .map(PollOptionResponseDto::new)
                .toList();
        this.hasVoted = currentVote != null;
        this.totalVoteCount = totalVoteCount;
        this.selectedOptionId = currentVote == null ? null : currentVote.getOptionId();
        this.result = currentVote == null
                ? null
                : new PollResultResponseDto(poll.getOptions(), voteCounts, totalVoteCount);
    }

    public static PollResponseDto withoutResult(Poll poll, long totalVoteCount) {
        return new PollResponseDto(poll, totalVoteCount, null, List.of());
    }

    public static PollResponseDto withResult(
            Poll poll,
            long totalVoteCount,
            PollVote currentVote,
            List<PollVoteCountProjection> voteCounts
    ) {
        return new PollResponseDto(poll, totalVoteCount, currentVote, voteCounts);
    }
}
