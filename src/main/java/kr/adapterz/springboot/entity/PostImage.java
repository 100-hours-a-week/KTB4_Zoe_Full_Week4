package kr.adapterz.springboot.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "post_images",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_images_post_order",
                columnNames = {"post_id", "image_order"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private boolean thumbnail;

    @Column(nullable = false)
    private int imageOrder;

    public PostImage(Post post, String imageUrl, boolean thumbnail, int imageOrder) {
        this.post = post;
        this.imageUrl = imageUrl;
        this.thumbnail = thumbnail;
        this.imageOrder = imageOrder;
    }
}
