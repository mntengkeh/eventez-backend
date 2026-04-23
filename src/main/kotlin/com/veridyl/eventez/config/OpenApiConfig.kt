package com.veridyl.eventez.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun eventEaseOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("EventEz API")
                    .description("Marketplace API connecting event planners with local service providers.")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Veridyl Support")
                            .email("support@veridyl.com")
                    )
            )
    }
}
