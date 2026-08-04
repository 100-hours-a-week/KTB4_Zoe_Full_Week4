package kr.adapterz.springboot.controller;

import jakarta.validation.Valid;
import kr.adapterz.springboot.dto.ApiResponseDto;
import kr.adapterz.springboot.dto.PollVoteCancelResponseDto;
import kr.adapterz.springboot.dto.PollVoteRequestDto;
import kr.adapterz.springboot.dto.PollVoteUpdateResponseDto;
import kr.adapterz.springboot.service.PollVoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/{postId}/poll/vote")
@RequiredArgsConstructor
public class PollVoteController {

    private final PollVoteService pollVoteService;

    @PutMapping
    public ResponseEntity<ApiResponseDto<PollVoteUpdateResponseDto>> vote(
            @PathVariable Long postId,
            @Valid @RequestBody PollVoteRequestDto request
    ) {
        PollVoteUpdateResponseDto response = pollVoteService.vote(postId, request);
        return ResponseEntity.ok(new ApiResponseDto<>("poll_vote_updated", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponseDto<PollVoteCancelResponseDto>> cancelVote(
            @PathVariable Long postId
    ) {
        PollVoteCancelResponseDto response = pollVoteService.cancelVote(postId);
        return ResponseEntity.ok(new ApiResponseDto<>("poll_vote_cancelled", response));
    }
}
