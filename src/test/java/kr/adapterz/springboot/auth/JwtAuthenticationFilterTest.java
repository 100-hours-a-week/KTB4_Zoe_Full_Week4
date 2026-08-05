package kr.adapterz.springboot.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAuthenticationFilterTest {

    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            mock(JwtTokenProvider.class)
    );

    @Test
    @DisplayName("공개 게시글 목록에서도 선택적 JWT 인증을 처리한다")
    void filterPublicPostList() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/posts");
        request.setServletPath("/posts");

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    @DisplayName("JWT 처리가 필요 없는 공개 경로는 필터에서 제외한다")
    void skipCsrfEndpoint() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/csrf");
        request.setServletPath("/csrf");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }
}
