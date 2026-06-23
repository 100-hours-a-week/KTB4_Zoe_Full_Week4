package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.auth.ForbiddenException;
import kr.adapterz.springboot.dto.CommentListResponseDto;
import kr.adapterz.springboot.dto.CommentRequestDto;
import kr.adapterz.springboot.dto.CommentResponseDto;
import kr.adapterz.springboot.dto.PaginationResponseDto;
import kr.adapterz.springboot.entity.Comment;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.exception.CommentNotFoundException;
import kr.adapterz.springboot.exception.ParentCommentMismatchException;
import kr.adapterz.springboot.exception.PostNotFoundException;
import kr.adapterz.springboot.exception.UserNotFoundException;
import kr.adapterz.springboot.repository.CommentRepository;
import kr.adapterz.springboot.repository.PostRepository;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public Comment createComment(Long postId, CommentRequestDto request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        User author = userRepository.findById(currentUserId)
                .orElseThrow(UserNotFoundException::new);

        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        Comment parent = findParentComment(request.getParentId(), postId);

        Comment comment = new Comment(
                request.getContent(),
                post,
                author,
                parent
        );

        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> getComments(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        return commentRepository.findByPostId(postId);
    }

    @Transactional(readOnly = true)
    public CommentListResponseDto getComments(Long postId, int page, int size) {
        postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        List<Comment> comments = commentRepository.findByPostId(postId);
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int startIndex = Math.min((safePage - 1) * safeSize, comments.size());
        int endIndex = Math.min(startIndex + safeSize, comments.size());
        boolean hasNext = endIndex < comments.size();

        List<CommentResponseDto> response = comments.subList(startIndex, endIndex).stream()
                .map(CommentResponseDto::new)
                .toList();

        return new CommentListResponseDto(response, new PaginationResponseDto(safePage, safeSize, hasNext));
    }

    @Transactional
    public Comment updateComment(Long commentId, CommentRequestDto request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        Long currentUserId = currentUserProvider.getCurrentUserId();

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("댓글 수정 권한이 없습니다.");
        }

        comment.changeContent(request.getContent());

        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        Long currentUserId = currentUserProvider.getCurrentUserId();

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("댓글 삭제 권한이 없습니다.");
        }

        comment.deleteKeepingThread();
        commentRepository.save(comment);
    }

    private Comment findParentComment(Long parentId, Long postId) {
        if (parentId == null) {
            return null;
        }

        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(CommentNotFoundException::new);

        if (!parent.getPost().getId().equals(postId)) {
            throw new ParentCommentMismatchException();
        }

        return parent;
    }
}
