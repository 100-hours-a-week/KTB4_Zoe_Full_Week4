package kr.adapterz.springboot.repository;

import java.time.LocalDateTime;

public record MyPageActivityCandidate(Long postId, LocalDateTime activityAt) {
}
