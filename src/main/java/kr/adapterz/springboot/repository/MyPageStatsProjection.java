package kr.adapterz.springboot.repository;

public record MyPageStatsProjection(
        long postCount,
        long pollParticipationCount,
        long receivedLikeCount
) {
}
