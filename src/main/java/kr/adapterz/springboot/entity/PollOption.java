package kr.adapterz.springboot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "poll_options",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_poll_options_poll_order", columnNames = {"poll_id", "option_order"}),
                @UniqueConstraint(name = "uk_poll_options_poll_id", columnNames = {"poll_id", "id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @Column(nullable = false, length = 30)
    private String content;

    @Column(name = "option_order", nullable = false)
    private int optionOrder;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    PollOption(Poll poll, String content, int optionOrder) {
        this.poll = poll;
        this.content = content;
        this.optionOrder = optionOrder;
        this.createdAt = LocalDateTime.now();
    }
}
