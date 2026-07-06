package kr.adapterz.springboot.auth;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

record PermitPath(HttpMethod method, PathPattern pattern) {

    private static final PathPatternParser PARSER = new PathPatternParser();

    static PermitPath get(String pattern) {
        return new PermitPath(HttpMethod.GET, PARSER.parse(pattern));
    }

    static PermitPath post(String pattern) {
        return new PermitPath(HttpMethod.POST, PARSER.parse(pattern));
    }

    static PermitPath options(String pattern) {
        return new PermitPath(HttpMethod.OPTIONS, PARSER.parse(pattern));
    }

    boolean matches(String requestMethod, String path) {
        return method.matches(requestMethod)
                && pattern.matches(PathContainer.parsePath(path));
    }
}