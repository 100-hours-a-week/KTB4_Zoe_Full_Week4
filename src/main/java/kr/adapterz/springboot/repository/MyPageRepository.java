package kr.adapterz.springboot.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.PostStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class MyPageRepository {

    private static final String ACTIVE_STATUS = "status";

    private final EntityManager entityManager;

    public List<MyPageActivityCandidate> findActivityCandidates(
            String tab,
            Long userId,
            LocalDateTime cursorAt,
            Long cursorPostId,
            int limit
    ) {
        String activityAtExpression;
        String fromClause;
        String activityCondition;

        switch (tab) {
            case "written" -> {
                activityAtExpression = "p.createdAt";
                fromClause = "Post p";
                activityCondition = "p.author.id = :userId";
            }
            case "participated" -> {
                activityAtExpression = "pv.updatedAt";
                fromClause = "PollVote pv join pv.poll.post p";
                activityCondition = "pv.user.id = :userId";
            }
            case "liked" -> {
                activityAtExpression = "l.createdAt";
                fromClause = "Like l join l.post p";
                activityCondition = "l.user.id = :userId";
            }
            default -> throw new IllegalArgumentException("지원하지 않는 마이페이지 활동 탭입니다.");
        }

        StringBuilder jpql = new StringBuilder()
                .append("select new kr.adapterz.springboot.repository.MyPageActivityCandidate(")
                .append("p.id, ")
                .append(activityAtExpression)
                .append(") from ")
                .append(fromClause)
                .append(" where ")
                .append(activityCondition)
                .append(" and p.status = :status");

        if (cursorAt != null && cursorPostId != null) {
            jpql.append(" and (")
                    .append(activityAtExpression).append(" < :cursorAt")
                    .append(" or (")
                    .append(activityAtExpression).append(" = :cursorAt")
                    .append(" and p.id < :cursorPostId")
                    .append("))");
        }

        jpql.append(" order by ")
                .append(activityAtExpression)
                .append(" desc, p.id desc");

        TypedQuery<MyPageActivityCandidate> query = entityManager.createQuery(
                jpql.toString(),
                MyPageActivityCandidate.class
        );
        query.setParameter("userId", userId);
        query.setParameter(ACTIVE_STATUS, PostStatus.ACTIVE);
        if (cursorAt != null && cursorPostId != null) {
            query.setParameter("cursorAt", cursorAt);
            query.setParameter("cursorPostId", cursorPostId);
        }
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<Post> findActivePostsByIds(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return List.of();
        }

        return entityManager.createQuery("""
                        select p
                        from Post p
                        where p.id in :postIds
                          and p.status = :status
                        """, Post.class)
                .setParameter("postIds", postIds)
                .setParameter(ACTIVE_STATUS, PostStatus.ACTIVE)
                .getResultList();
    }

    public Map<Long, Long> findLikeCounts(List<Long> postIds) {
        return countByPostId("""
                select l.post.id, count(l)
                from Like l
                where l.post.id in :postIds
                group by l.post.id
                """, postIds);
    }

    public Map<Long, Long> findCommentCounts(List<Long> postIds) {
        return countByPostId("""
                select c.post.id, count(c)
                from Comment c
                where c.post.id in :postIds
                group by c.post.id
                """, postIds);
    }

    public Map<Long, Long> findParticipantCounts(List<Long> postIds) {
        return countByPostId("""
                select pv.poll.postId, count(pv)
                from PollVote pv
                where pv.poll.postId in :postIds
                group by pv.poll.postId
                """, postIds);
    }

    public Set<Long> findVotedPostIds(Long userId, List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Set.of();
        }

        return new HashSet<>(entityManager.createQuery("""
                        select pv.poll.postId
                        from PollVote pv
                        where pv.user.id = :userId
                          and pv.poll.postId in :postIds
                        """, Long.class)
                .setParameter("userId", userId)
                .setParameter("postIds", postIds)
                .getResultList());
    }

    public MyPageStatsProjection findStats(Long userId) {
        long postCount = entityManager.createQuery("""
                        select count(p)
                        from Post p
                        where p.author.id = :userId
                          and p.status = :status
                        """, Long.class)
                .setParameter("userId", userId)
                .setParameter(ACTIVE_STATUS, PostStatus.ACTIVE)
                .getSingleResult();

        long pollParticipationCount = entityManager.createQuery("""
                        select count(pv)
                        from PollVote pv
                        join pv.poll.post p
                        where pv.user.id = :userId
                          and p.status = :status
                        """, Long.class)
                .setParameter("userId", userId)
                .setParameter(ACTIVE_STATUS, PostStatus.ACTIVE)
                .getSingleResult();

        long receivedLikeCount = entityManager.createQuery("""
                        select count(l)
                        from Like l
                        join l.post p
                        where p.author.id = :userId
                          and p.status = :status
                        """, Long.class)
                .setParameter("userId", userId)
                .setParameter(ACTIVE_STATUS, PostStatus.ACTIVE)
                .getSingleResult();

        return new MyPageStatsProjection(
                postCount,
                pollParticipationCount,
                receivedLikeCount
        );
    }

    private Map<Long, Long> countByPostId(String jpql, List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> counts = new HashMap<>();
        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setParameter("postIds", postIds)
                .getResultList();
        for (Object[] row : rows) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }
}
