package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.PollVoteRequestDto;
import kr.adapterz.springboot.dto.PollVoteUpdateResponseDto;
import kr.adapterz.springboot.entity.Poll;
import kr.adapterz.springboot.entity.PollOption;
import kr.adapterz.springboot.entity.PollVote;
import kr.adapterz.springboot.entity.PollVoteId;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.DeletedUserException;
import kr.adapterz.springboot.exception.PollNotFoundException;
import kr.adapterz.springboot.exception.PollOptionMismatchException;
import kr.adapterz.springboot.exception.PostBlindedException;
import kr.adapterz.springboot.exception.UserNotFoundException;
import kr.adapterz.springboot.repository.PollOptionRepository;
import kr.adapterz.springboot.repository.PollRepository;
import kr.adapterz.springboot.repository.PollVoteCountProjection;
import kr.adapterz.springboot.repository.PollVoteRepository;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PollVoteService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public PollVoteUpdateResponseDto vote(Long postId, PollVoteRequestDto request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(UserNotFoundException::new);
        if (currentUser.isDeleted()) {
            throw new DeletedUserException();
        }

        Poll lockedPoll = pollRepository.findByPostIdForParticipation(postId)
                .orElseThrow(PollNotFoundException::new);
        if (lockedPoll.getPost().isDeleted()) {
            throw new PollNotFoundException();
        }
        if (lockedPoll.getPost().isBlinded()) {
            throw new PostBlindedException();
        }

        PollOption selectedOption = pollOptionRepository.findByIdAndPollPostId(
                        request.getOptionId(),
                        lockedPoll.getPostId()
                )
                .orElseThrow(PollOptionMismatchException::new);

        PollVote currentVote = pollVoteRepository.findById(
                        new PollVoteId(lockedPoll.getPostId(), currentUser.getId())
                )
                .orElse(null);

        if (currentVote == null || !currentVote.getOptionId().equals(selectedOption.getId())) {
            pollVoteRepository.upsertVote(
                    lockedPoll.getPostId(),
                    currentUser.getId(),
                    selectedOption.getId()
            );
        }

        Poll pollWithOptions = pollRepository.findByPostIdWithOptions(lockedPoll.getPostId())
                .orElseThrow(PollNotFoundException::new);
        List<PollVoteCountProjection> voteCounts =
                pollVoteRepository.countVotesByOption(lockedPoll.getPostId());
        long totalVoteCount = voteCounts.stream()
                .mapToLong(PollVoteCountProjection::getVoteCount)
                .sum();

        return new PollVoteUpdateResponseDto(
                pollWithOptions,
                selectedOption.getId(),
                totalVoteCount,
                voteCounts
        );
    }
}
