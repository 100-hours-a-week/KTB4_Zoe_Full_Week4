package kr.adapterz.springboot.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "post_drafts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_drafts_user_draft_key",
                        columnNames = {"user_id", "draft_key"}
                ),
                @UniqueConstraint(
                        name = "uk_post_drafts_user_post",
                        columnNames = {"user_id", "post_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false, length = 64)
    private String draftKey;

    @Column(length = 26)
    private String title;

    @Column(columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public PostDraft(User user, Post post, String draftKey, String title, String content) {
        this.user = user;
        this.post = post;
        this.draftKey = draftKey;
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getPostId() {
        return post == null ? null : post.getId();
    }
}
