package kr.adapterz.springboot.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@Table(name = "polls")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Poll {

    private static final int MIN_OPTION_COUNT = 2;
    private static final int MAX_OPTION_COUNT = 5;
    private static final int MAX_OPTION_LENGTH = 30;

    @Id
    @Column(name = "post_id")
    private Long postId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionOrder ASC")
    private List<PollOption> options = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Poll(Post post, List<String> optionContents) {
        if (post == null) {
            throw new IllegalArgumentException("게시글은 필수입니다.");
        }

        List<String> normalizedOptions = normalizeOptions(optionContents);
        LocalDateTime now = LocalDateTime.now();

        this.post = post;
        this.createdAt = now;
        this.updatedAt = now;

        for (int order = 0; order < normalizedOptions.size(); order++) {
            this.options.add(new PollOption(this, normalizedOptions.get(order), order));
        }
    }

    public List<PollOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    private List<String> normalizeOptions(List<String> optionContents) {
        if (optionContents == null
                || optionContents.size() < MIN_OPTION_COUNT
                || optionContents.size() > MAX_OPTION_COUNT) {
            throw new IllegalArgumentException("선택지는 2개 이상 5개 이하여야 합니다.");
        }

        return optionContents.stream()
                .map(this::normalizeOption)
                .toList();
    }

    private String normalizeOption(String optionContent) {
        if (optionContent == null) {
            throw new IllegalArgumentException("선택지는 필수입니다.");
        }

        String normalizedContent = optionContent.strip();
        if (normalizedContent.isEmpty() || normalizedContent.length() > MAX_OPTION_LENGTH) {
            throw new IllegalArgumentException("각 선택지는 앞뒤 공백 제거 후 1자 이상 30자 이하여야 합니다.");
        }

        return normalizedContent;
    }
}
