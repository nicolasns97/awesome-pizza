package com.adesso.awesomepizza.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class KitchenTokenInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Kitchen-Token";

    private static final Logger log = LoggerFactory.getLogger(KitchenTokenInterceptor.class);

    private final byte[] expectedToken;

    public KitchenTokenInterceptor(KitchenAuthProperties properties) {
        this.expectedToken = properties.token().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod method && method.hasMethodAnnotation(KitchenOnly.class)) {
            String token = request.getHeader(HEADER);
            if (token == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), expectedToken)) {
                log.warn("Rejected kitchen request method={} path={} remoteAddress={} tokenPresent={}",
                        request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), token != null);
                throw new InvalidKitchenTokenException();
            }
        }
        return true;
    }
}
