package kr.adapterz.springboot.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "post_versions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_versions_post_version",
                columnNames = {"post_id", "post_version"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 26)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private int postVersion;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public PostVersion(Post post, User user, String title, String content, int postVersion) {
        this.post = post;
        this.user = user;
        this.title = title;
        this.content = content;
        this.postVersion = postVersion;
        this.createdAt = LocalDateTime.now();
    }
}
