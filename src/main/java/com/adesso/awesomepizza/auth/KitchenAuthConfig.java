package com.adesso.awesomepizza.auth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(KitchenAuthProperties.class)
public class KitchenAuthConfig implements WebMvcConfigurer {

    private final KitchenTokenInterceptor kitchenTokenInterceptor;

    public KitchenAuthConfig(KitchenTokenInterceptor kitchenTokenInterceptor) {
        this.kitchenTokenInterceptor = kitchenTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(kitchenTokenInterceptor).addPathPatterns("/api/**");
    }
}
