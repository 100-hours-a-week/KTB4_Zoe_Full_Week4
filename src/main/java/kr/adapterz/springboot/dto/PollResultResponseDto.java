package kr.adapterz.springboot.dto;

import kr.adapterz.springboot.entity.PollOption;
import kr.adapterz.springboot.repository.PollVoteCountProjection;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class PollResultResponseDto {

    private final List<PollResultOptionResponseDto> options;

    public PollResultResponseDto(
            List<PollOption> pollOptions,
            List<PollVoteCountProjection> voteCounts,
            long totalVoteCount
    ) {
        Map<Long, Long> voteCountByOptionId = voteCounts.stream()
                .collect(Collectors.toMap(
                        PollVoteCountProjection::getOptionId,
                        PollVoteCountProjection::getVoteCount
                ));

        this.options = pollOptions.stream()
                .map(option -> new PollResultOptionResponseDto(
                        option.getId(),
                        voteCountByOptionId.getOrDefault(option.getId(), 0L),
                        totalVoteCount
                ))
                .toList();
    }
}
