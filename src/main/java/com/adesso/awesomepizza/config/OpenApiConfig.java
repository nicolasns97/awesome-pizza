package com.adesso.awesomepizza.config;

import com.adesso.awesomepizza.auth.KitchenOnly;
import com.adesso.awesomepizza.auth.KitchenTokenInterceptor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String KITCHEN_TOKEN_SCHEME = "kitchenToken";

    @Bean
    public OpenAPI awesomePizzaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Awesome Pizza API")
                        .version("v1")
                        .description("Customers place, track and cancel orders; the kitchen lists, takes and completes them."))
                .components(new Components().addSecuritySchemes(KITCHEN_TOKEN_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name(KitchenTokenInterceptor.HEADER)
                        .description("Kitchen token, required by kitchen endpoints.")));
    }

    @Bean
    public OperationCustomizer kitchenOnlySecurity() {
        return (operation, handlerMethod) -> handlerMethod.hasMethodAnnotation(KitchenOnly.class)
                ? operation.addSecurityItem(new SecurityRequirement().addList(KITCHEN_TOKEN_SCHEME))
                : operation;
    }
}
