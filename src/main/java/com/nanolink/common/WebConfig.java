package com.nanolink.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Add CORS configuration to allow frontend integration
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173", "http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        registry.addMapping("/swagger-ui/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS");

        registry.addMapping("/v3/api-docs**")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS");
    }

    /**
     * Add rate limiting interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitingInterceptor());
    }

    /**
     * Simple rate limiting interceptor using sliding window counter
     * - Auth endpoints: 10 requests per minute per IP
     * - Other endpoints: 100 requests per minute per IP
     */
    public static class RateLimitingInterceptor implements HandlerInterceptor {
        private final Map<String, RequestCounter> cache = new ConcurrentHashMap<>();
        private static final int AUTH_REQUESTS_PER_MINUTE = 10;
        private static final int DEFAULT_REQUESTS_PER_MINUTE = 100;
        private static final long MINUTE_MILLIS = 60 * 1000;

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String clientIp = getClientIp(request);
            String path = request.getRequestURI();

            // Determine rate limit based on endpoint
            int limit = path.contains("/api/auth/") ? AUTH_REQUESTS_PER_MINUTE : DEFAULT_REQUESTS_PER_MINUTE;
            RequestCounter counter = cache.computeIfAbsent(clientIp, ip -> new RequestCounter(limit));

            if (counter.allowRequest(limit)) {
                // Request allowed
                response.addHeader("X-Rate-Limit-Remaining", String.valueOf(Math.max(0, limit - counter.getRequestCount())));
                return true;
            } else {
                // Rate limit exceeded (429 Too Many Requests)
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Rate limit exceeded. Max " + limit + " requests per minute.\"}");
                response.addHeader("Retry-After", "60");
                return false;
            }
        }

        /**
         * Extract client IP from request (handles X-Forwarded-For and proxies)
         */
        private String getClientIp(HttpServletRequest request) {
            String clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp != null && !clientIp.isEmpty()) {
                return clientIp.split(",")[0].trim();
            }
            clientIp = request.getHeader("X-Real-IP");
            if (clientIp != null && !clientIp.isEmpty()) {
                return clientIp;
            }
            return request.getRemoteAddr();
        }

        /**
         * Request counter with sliding window
         */
        private static class RequestCounter {
            private Queue<Long> timestamps = new LinkedList<>();
            private final int limit;

            RequestCounter(int limit) {
                this.limit = limit;
            }

            synchronized boolean allowRequest(int limit) {
                long now = System.currentTimeMillis();
                // Remove timestamps older than 1 minute
                while (!timestamps.isEmpty() && timestamps.peek() < now - MINUTE_MILLIS) {
                    timestamps.poll();
                }

                if (timestamps.size() < limit) {
                    timestamps.add(now);
                    return true;
                }
                return false;
            }

            synchronized int getRequestCount() {
                long now = System.currentTimeMillis();
                while (!timestamps.isEmpty() && timestamps.peek() < now - MINUTE_MILLIS) {
                    timestamps.poll();
                }
                return timestamps.size();
            }
        }
    }
}
