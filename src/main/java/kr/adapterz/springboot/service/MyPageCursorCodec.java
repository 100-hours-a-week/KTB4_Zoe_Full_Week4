package kr.adapterz.springboot.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.adapterz.springboot.exception.InvalidCursorException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MyPageCursorCodec {

    private static final Set<String> ALLOWED_FIELDS = Set.of("tab", "activity_at", "post_id");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectMapper objectMapper;

    public String encode(String tab, LocalDateTime activityAt, Long postId) {
        try {
            CursorPayload payload = new CursorPayload(
                    tab,
                    activityAt.format(DATE_TIME_FORMATTER),
                    postId
            );
            byte[] json = objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (RuntimeException e) {
            throw new IllegalStateException("마이페이지 cursor 생성에 실패했습니다.", e);
        }
    }

    public DecodedCursor decode(String encodedCursor, String requestedTab) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            throw new InvalidCursorException();
        }

        try {
            byte[] json = Base64.getUrlDecoder().decode(encodedCursor);
            String jsonText = new String(json, StandardCharsets.UTF_8);
            Map<?, ?> rawPayload = objectMapper.readValue(jsonText, Map.class);
            if (rawPayload.size() != ALLOWED_FIELDS.size()
                    || !rawPayload.keySet().stream()
                    .allMatch(key -> key instanceof String && ALLOWED_FIELDS.contains(key))) {
                throw new InvalidCursorException();
            }
            CursorPayload payload = objectMapper.readValue(jsonText, CursorPayload.class);

            if (payload.tab() == null
                    || !payload.tab().equals(requestedTab)
                    || payload.activityAt() == null
                    || payload.postId() == null
                    || payload.postId() <= 0) {
                throw new InvalidCursorException();
            }

            LocalDateTime activityAt = LocalDateTime.parse(
                    payload.activityAt(),
                    DATE_TIME_FORMATTER
            );
            return new DecodedCursor(payload.tab(), activityAt, payload.postId());
        } catch (InvalidCursorException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new InvalidCursorException();
        }
    }

    public record DecodedCursor(String tab, LocalDateTime activityAt, Long postId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    private record CursorPayload(
            String tab,
            @JsonProperty("activity_at") String activityAt,
            @JsonProperty("post_id") Long postId
    ) {
    }
}
