package com.moonlight.stays.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter implements Filter {

    private final Map<String, AtomicInteger> requestCountsPerIp = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String ip = httpRequest.getRemoteAddr();

        requestCountsPerIp.putIfAbsent(ip, new AtomicInteger(0));
        int requests = requestCountsPerIp.get(ip).incrementAndGet();

        if (requests > MAX_REQUESTS_PER_MINUTE) {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"message\": \"Rate limit exceeded. Maximum 60 requests per minute allowed.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    // Task to periodically clear rate logs
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)
    public void resetCounts() {
        requestCountsPerIp.clear();
    }
}
