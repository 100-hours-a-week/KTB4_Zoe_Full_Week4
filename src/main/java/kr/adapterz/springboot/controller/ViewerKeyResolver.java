package kr.adapterz.springboot.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ViewerKeyResolver {

    public String createGuestViewerKey(HttpServletRequest request) {
        String userAgent = Objects.toString(request.getHeader("User-Agent"), "");
        return "GUEST:" + getClientIp(request) + ":" + Integer.toHexString(userAgent.hashCode());
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
