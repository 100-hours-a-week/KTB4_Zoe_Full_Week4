package kr.adapterz.springboot.service;

import kr.adapterz.springboot.exception.InvalidCursorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class MyPageCursorCodecTest {

    @Autowired
    private MyPageCursorCodec cursorCodec;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Cursor를 URL-safe Base64 JSON으로 발급하고 다시 읽는다")
    void roundTrip() {
        String encoded = cursorCodec.encode(
                "written",
                LocalDateTime.of(2026, 8, 6, 10, 20),
                101L
        );

        MyPageCursorCodec.DecodedCursor decoded = cursorCodec.decode(encoded, "written");

        assertThat(decoded.tab()).isEqualTo("written");
        assertThat(decoded.activityAt()).isEqualTo(LocalDateTime.of(2026, 8, 6, 10, 20));
        assertThat(decoded.postId()).isEqualTo(101L);
        assertThat(encoded).doesNotContain("=");
    }

    @Test
    @DisplayName("알 수 없는 Cursor 필드는 거부한다")
    void rejectUnknownField() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tab", "written");
        payload.put("activity_at", "2026-08-06 10:20:00");
        payload.put("post_id", 101);
        payload.put("extra", "not-allowed");
        String encoded = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(objectMapper.writeValueAsString(payload)
                        .getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> cursorCodec.decode(encoded, "written"))
                .isInstanceOf(InvalidCursorException.class);
    }

    @Test
    @DisplayName("탭이 다른 Cursor는 거부한다")
    void rejectCursorForDifferentTab() {
        String encoded = cursorCodec.encode(
                "written",
                LocalDateTime.of(2026, 8, 6, 10, 20),
                101L
        );

        assertThatThrownBy(() -> cursorCodec.decode(encoded, "liked"))
                .isInstanceOf(InvalidCursorException.class);
    }
}
