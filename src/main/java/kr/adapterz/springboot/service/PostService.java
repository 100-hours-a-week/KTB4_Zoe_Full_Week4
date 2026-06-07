package kr.adapterz.springboot.service;

import kr.adapterz.springboot.auth.CurrentUserProvider;
import kr.adapterz.springboot.auth.ForbiddenException;
import kr.adapterz.springboot.dto.PostRequestDto;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import kr.adapterz.springboot.repository.PostRepository;
import kr.adapterz.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public Post createPost(PostRequestDto request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Post post = new Post(
                request.getTitle(),
                request.getContent(),
                author
        );

        return postRepository.save(post);
    }

    public List<Post> getPosts() {
        return postRepository.findAll();
    }

    public Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    public Post updatePost(Long postId, PostRequestDto request) {
        Post post = getPost(postId);

        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("게시글 수정 권한이 없습니다.");
        }

        post.changeTitle(request.getTitle());
        post.changeContent(request.getContent());

        return post;
    }

    public void deletePost(Long postId) {
        Post post = getPost(postId);

        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("게시글 삭제 권한이 없습니다.");
        }

        boolean deleted = postRepository.deleteById(postId);

        if (!deleted) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }
    }
}