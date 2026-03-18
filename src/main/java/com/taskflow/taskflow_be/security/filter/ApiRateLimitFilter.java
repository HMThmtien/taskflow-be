package com.taskflow.taskflow_be.security.filter;

import com.taskflow.taskflow_be.config.web.RequestCorrelationFilter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApiRateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean isSearch = "GET".equalsIgnoreCase(request.getMethod()) && "/api/search".equals(uri);
        boolean isCommentCreate = "POST".equalsIgnoreCase(request.getMethod()) && uri.matches("^/api/issues/[^/]+/comments$");
        return !(isSearch || isCommentCreate);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String key = routeKey(request) + ":" + clientKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> newBucket(request));

        if (!bucket.tryConsume(1)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, String.valueOf(request.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTR)));
            response.getWriter().write("""
                {"success":false,"requestId":"%s","error":{"code":"TOO_MANY_REQUESTS","message":"Too many requests, please slow down and try again shortly","details":null}}
                """.formatted(String.valueOf(request.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTR))));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Bucket newBucket(HttpServletRequest request) {
        if ("/api/search".equals(request.getRequestURI())) {
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1))))
                    .build();
        }

        return Bucket.builder()
                .addLimit(Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1))))
                .build();
    }

    private String routeKey(HttpServletRequest request) {
        return "/api/search".equals(request.getRequestURI()) ? "search" : "issue-comment-create";
    }

    private String clientKey(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
