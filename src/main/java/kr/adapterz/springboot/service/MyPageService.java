package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.MyPageActivitiesQuery;
import kr.adapterz.springboot.dto.MyPageActivityItemResponseDto;
import kr.adapterz.springboot.dto.MyPageActivityResponseDto;
import kr.adapterz.springboot.dto.MyPageProfileResponseDto;
import kr.adapterz.springboot.dto.MyPageQuery;
import kr.adapterz.springboot.dto.MyPageResponseDto;
import kr.adapterz.springboot.dto.MyPageStatsResponseDto;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.InvalidCursorException;
import kr.adapterz.springboot.exception.MyPageReadFailedException;
import kr.adapterz.springboot.repository.MyPageActivityCandidate;
import kr.adapterz.springboot.repository.MyPageRepository;
import kr.adapterz.springboot.repository.MyPageStatsProjection;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final MyPageRepository myPageRepository;
    private final MyPageCursorCodec cursorCodec;

    @Transactional(readOnly = true)
    public MyPageResponseDto getMyPage(MyPageQuery query) {
        Long userId = currentUserProvider.getCurrentUserId();
        try {
            User user = getActiveUser(userId);
            MyPageStatsProjection stats = myPageRepository.findStats(userId);
            MyPageActivityResponseDto activity = getActivityPage(
                    query.getTab(),
                    query.getSize(),
                    query.getCursor(),
                    userId
            );

            return new MyPageResponseDto(
                    new MyPageProfileResponseDto(user),
                    new MyPageStatsResponseDto(
                            stats.postCount(),
                            stats.pollParticipationCount(),
                            stats.receivedLikeCount()
                    ),
                    activity
            );
        } catch (InvalidCursorException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new MyPageReadFailedException(e);
        }
    }

    @Transactional(readOnly = true)
    public MyPageActivityResponseDto getActivities(MyPageActivitiesQuery query) {
        Long userId = currentUserProvider.getCurrentUserId();
        try {
            getActiveUser(userId);
            return getActivityPage(
                    query.getTab(),
                    query.getSize(),
                    query.getCursor(),
                    userId
            );
        } catch (InvalidCursorException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new MyPageReadFailedException(e);
        }
    }

    private User getActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(kr.adapterz.springboot.exception.UserNotFoundException::new);
        if (user.isDeleted()) {
            throw new kr.adapterz.springboot.exception.DeletedUserException();
        }
        return user;
    }

    private MyPageActivityResponseDto getActivityPage(
            String tab,
            int size,
            String encodedCursor,
            Long userId
    ) {
        MyPageCursorCodec.DecodedCursor cursor = null;
        if (encodedCursor != null) {
            cursor = cursorCodec.decode(encodedCursor, tab);
        }

        List<MyPageActivityCandidate> candidates = myPageRepository.findActivityCandidates(
                tab,
                userId,
                cursor == null ? null : cursor.activityAt(),
                cursor == null ? null : cursor.postId(),
                size + 1
        );

        boolean hasNext = candidates.size() > size;
        List<MyPageActivityCandidate> pageCandidates = candidates.stream()
                .limit(size)
                .toList();

        List<Long> postIds = pageCandidates.stream()
                .map(MyPageActivityCandidate::postId)
                .toList();
        List<Post> posts = myPageRepository.findActivePostsByIds(postIds);
        Map<Long, Post> postsById = new HashMap<>();
        posts.forEach(post -> postsById.put(post.getId(), post));

        Map<Long, Long> likeCounts = myPageRepository.findLikeCounts(postIds);
        Map<Long, Long> commentCounts = myPageRepository.findCommentCounts(postIds);
        Map<Long, Long> participantCounts = myPageRepository.findParticipantCounts(postIds);
        Set<Long> votedPostIds = myPageRepository.findVotedPostIds(userId, postIds);

        List<MyPageActivityItemResponseDto> items = pageCandidates.stream()
                .filter(candidate -> postsById.containsKey(candidate.postId()))
                .map(candidate -> toActivityItem(
                        candidate,
                        postsById.get(candidate.postId()),
                        likeCounts,
                        commentCounts,
                        participantCounts,
                        votedPostIds
                ))
                .toList();

        String nextCursor = hasNext && !pageCandidates.isEmpty()
                ? cursorCodec.encode(
                        tab,
                        pageCandidates.getLast().activityAt(),
                        pageCandidates.getLast().postId()
                )
                : null;

        return new MyPageActivityResponseDto(tab, items, nextCursor, hasNext);
    }

    private MyPageActivityItemResponseDto toActivityItem(
            MyPageActivityCandidate candidate,
            Post post,
            Map<Long, Long> likeCounts,
            Map<Long, Long> commentCounts,
            Map<Long, Long> participantCounts,
            Set<Long> votedPostIds
    ) {
        return new MyPageActivityItemResponseDto(
                post.getId(),
                post.getTitle(),
                candidate.activityAt(),
                post.getCreatedAt(),
                likeCounts.getOrDefault(post.getId(), 0L),
                commentCounts.getOrDefault(post.getId(), 0L),
                participantCounts.getOrDefault(post.getId(), 0L),
                votedPostIds.contains(post.getId())
        );
    }
}
