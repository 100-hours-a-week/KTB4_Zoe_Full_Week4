package kr.adapterz.springboot.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.adapterz.springboot.exception.ErrorResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class SecurityAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorResponseFactory errorResponseFactory;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        ResponseEntity<?> error = errorResponseFactory.error(
                request,
                org.springframework.http.HttpStatus.FORBIDDEN,
                "access_denied"
        );
        response.setStatus(error.getStatusCode().value());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                objectMapper.writeValueAsString(error.getBody())
        );
    }
}
