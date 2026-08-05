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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void replaceOptions(List<OptionUpdate> requestedOptions) {
        if (requestedOptions == null
                || requestedOptions.size() < MIN_OPTION_COUNT
                || requestedOptions.size() > MAX_OPTION_COUNT) {
            throw new IllegalArgumentException("선택지는 2개 이상 5개 이하여야 합니다.");
        }

        Map<Long, PollOption> existingOptions = options.stream()
                .filter(option -> option.getId() != null)
                .collect(java.util.stream.Collectors.toMap(PollOption::getId, option -> option));
        Map<Long, Boolean> seenOptionIds = new HashMap<>();
        List<PollOption> orderedOptions = new ArrayList<>();

        for (int order = 0; order < requestedOptions.size(); order++) {
            OptionUpdate requestedOption = requestedOptions.get(order);
            String normalizedContent = normalizeOption(requestedOption.content());
            PollOption option;

            if (requestedOption.optionId() == null) {
                option = new PollOption(this, normalizedContent, order);
            } else {
                if (seenOptionIds.put(requestedOption.optionId(), Boolean.TRUE) != null) {
                    throw new IllegalArgumentException("선택지 식별자가 중복되었습니다.");
                }
                option = existingOptions.get(requestedOption.optionId());
                if (option == null) {
                    throw new IllegalArgumentException("다른 투표의 선택지는 사용할 수 없습니다.");
                }
                option.changeContent(normalizedContent);
                option.changeOrder(order);
            }

            orderedOptions.add(option);
        }

        options.removeIf(option -> !orderedOptions.contains(option));
        for (PollOption option : orderedOptions) {
            if (!options.contains(option)) {
                options.add(option);
            }
        }
        options.sort(java.util.Comparator.comparingInt(PollOption::getOptionOrder));
        updatedAt = LocalDateTime.now();
    }

    public record OptionUpdate(Long optionId, String content) {
    }
}
