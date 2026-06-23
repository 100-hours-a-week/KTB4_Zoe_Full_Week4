package kr.adapterz.springboot.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 26)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Column(nullable = false)
    private long viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("imageOrder ASC")
    private List<PostImage> images = new ArrayList<>();

    private LocalDateTime deletedAt;

    public Post(String title, String content, User author) {
        this.title =  title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.status = PostStatus.ACTIVE;
        this.viewCount = 0L;
        this.author = author;
    }

    public void  changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    //게시글 생성시 id 부여 메서드
    public void assignId(Long id) {
        this.id = id;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void delete() {
        this.status = PostStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void blind() {
        this.status = PostStatus.BLINDED;
    }

    public boolean isBlinded() {
        return status == PostStatus.BLINDED;
    }

    public void replaceImages(List<String> imageUrls) {
        this.images.clear();

        List<String> safeImageUrls = imageUrls == null ? List.of() : imageUrls;

        for (int i = 0; i < safeImageUrls.size(); i++) {
            this.images.add(new PostImage(this, safeImageUrls.get(i), i == 0, i));
        }
    }

    public String getThumbnailUrl() {
        return images.stream()
                .filter(PostImage::isThumbnail)
                .findFirst()
                .or(() -> images.stream().findFirst())
                .map(PostImage::getImageUrl)
                .orElse(null);
    }
}
