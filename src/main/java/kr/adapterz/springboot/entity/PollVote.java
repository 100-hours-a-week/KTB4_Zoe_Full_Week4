package kr.adapterz.springboot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "poll_votes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PollVote {

    @EmbeddedId
    private PollVoteId id;

    @MapsId("pollId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "option_id", nullable = false)
    private Long optionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(
                    name = "poll_id",
                    referencedColumnName = "poll_id",
                    insertable = false,
                    updatable = false
            ),
            @JoinColumn(
                    name = "option_id",
                    referencedColumnName = "id",
                    insertable = false,
                    updatable = false
            )
    })
    private PollOption option;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public PollVote(Poll poll, User user, PollOption option) {
        if (poll == null || user == null || option == null) {
            throw new IllegalArgumentException("투표, 사용자와 선택지는 필수입니다.");
        }
        if (!option.belongsTo(poll)) {
            throw new IllegalArgumentException("선택지가 해당 투표에 속하지 않습니다.");
        }

        this.id = new PollVoteId(poll.getPostId(), user.getId());
        this.poll = poll;
        this.user = user;
        this.optionId = option.getId();
        this.option = option;

        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
