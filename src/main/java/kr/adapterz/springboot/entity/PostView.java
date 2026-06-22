package kr.adapterz.springboot.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "post_views",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_views_post_viewer",
                columnNames = {"post_id", "viewer_key"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 255)
    private String viewerKey;

    @Column(nullable = false)
    private LocalDateTime lastViewedAt;

    public PostView(Post post, User user, String viewerKey, LocalDateTime lastViewedAt) {
        this.post = post;
        this.user = user;
        this.viewerKey = viewerKey;
        this.lastViewedAt = lastViewedAt;
    }

    public boolean canIncreaseViewCount(LocalDateTime now) {
        return lastViewedAt.plusHours(24).isBefore(now) || lastViewedAt.plusHours(24).isEqual(now);
    }

    public void updateLastViewedAt(LocalDateTime lastViewedAt) {
        this.lastViewedAt = lastViewedAt;
    }
}
