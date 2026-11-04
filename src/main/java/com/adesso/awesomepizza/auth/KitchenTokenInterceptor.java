package com.adesso.awesomepizza.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class KitchenTokenInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Kitchen-Token";

    private final byte[] expectedToken;

    public KitchenTokenInterceptor(KitchenAuthProperties properties) {
        this.expectedToken = properties.token().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod method && method.hasMethodAnnotation(KitchenOnly.class)) {
            String token = request.getHeader(HEADER);
            if (token == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), expectedToken)) {
                throw new InvalidKitchenTokenException();
            }
        }
        return true;
    }
}
