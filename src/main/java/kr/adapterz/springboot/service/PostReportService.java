package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.dto.PostReportRequestDto;
import kr.adapterz.springboot.dto.PostReportResponseDto;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.PostReport;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.DeletedUserException;
import kr.adapterz.springboot.exception.DuplicatePostReportException;
import kr.adapterz.springboot.exception.PostBlindedException;
import kr.adapterz.springboot.repository.PostReportRepository;
import kr.adapterz.springboot.repository.PostRepository;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostReportService {

    private static final long AUTO_BLIND_REPORT_COUNT = 5L;

    private final PostReportRepository postReportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public PostReportResponseDto reportPost(Long postId, PostReportRequestDto request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        User reporter = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (reporter.isDeleted()) {
            throw new DeletedUserException();
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (post.isBlinded()) {
            throw new PostBlindedException();
        }

        if (postReportRepository.existsByPostIdAndUserId(postId, currentUserId)) {
            throw new DuplicatePostReportException();
        }

        postReportRepository.save(new PostReport(post, reporter, request.getReason()));

        long reportCount = postReportRepository.countByPostId(postId);
        boolean blinded = reportCount >= AUTO_BLIND_REPORT_COUNT;

        if (blinded) {
            post.blind();
        }

        return new PostReportResponseDto(postId, reportCount, blinded);
    }
}
