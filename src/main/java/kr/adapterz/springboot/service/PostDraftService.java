package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.auth.ForbiddenException;
import kr.adapterz.springboot.dto.PostDraftRequestDto;
import kr.adapterz.springboot.dto.PostDraftResponseDto;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.PostDraft;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.repository.PostDraftRepository;
import kr.adapterz.springboot.repository.PostRepository;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostDraftService {

    private static final String NEW_POST_DRAFT_KEY = "NEW";

    private final PostDraftRepository postDraftRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public PostDraftResponseDto saveDraft(PostDraftRequestDto request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Post post = findPostForDraft(request.getPostId(), currentUserId);
        String draftKey = createDraftKey(request.getPostId());

        PostDraft draft = postDraftRepository.findByUserIdAndDraftKey(currentUserId, draftKey)
                .orElseGet(() -> new PostDraft(
                        user,
                        post,
                        draftKey,
                        request.getTitle(),
                        request.getContent()
                ));

        if (draft.getId() != null) {
            draft.update(request.getTitle(), request.getContent());
        }

        PostDraft savedDraft = postDraftRepository.save(draft);
        return new PostDraftResponseDto(savedDraft);
    }

    @Transactional(readOnly = true)
    public PostDraftResponseDto getDraft(Long postId) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        String draftKey = createDraftKey(postId);

        PostDraft draft = postDraftRepository.findByUserIdAndDraftKey(currentUserId, draftKey)
                .orElseThrow(() -> new IllegalArgumentException("임시저장을 찾을 수 없습니다."));

        return new PostDraftResponseDto(draft);
    }

    private Post findPostForDraft(Long postId, Long currentUserId) {
        if (postId == null) {
            return null;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("게시글 임시저장 권한이 없습니다.");
        }

        return post;
    }

    private String createDraftKey(Long postId) {
        return postId == null ? NEW_POST_DRAFT_KEY : String.valueOf(postId);
    }
}
