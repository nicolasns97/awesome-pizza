package com.adesso.awesomepizza.auth;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("awesome-pizza.auth.kitchen")
public record KitchenAuthProperties(
        @NotBlank(message = "The kitchen token must be configured (awesome-pizza.auth.kitchen.token).")
        String token
) {}
