package com.todoproject.todo_app.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()  // Это создание шапки которую видим в самом верху Swagger UI:
                        .title("TODO App API")
                        .description("REST API для управления задачами")
                        .version("1.0.0"))
                // Добавляем поддержку JWT авторизации в UI
                .addSecurityItem(new SecurityRequirement()  // говорит, что API требует авторизации и нужно использ эту схему с названием..
                        .addList("Bearer Authentication")) // просто название (имя) схемы авторизации, и оно должно совпадать с addSecuritySchemes ниже
                .components(new Components() // описывает как именно авторизоваться (Bearer JWT)
                        .addSecuritySchemes("Bearer Authentication", // описывает: "это HTTP Bearer JWT токен"
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))); // формат токена — JWT (подсказка для UI)
    } // это приводит к появлению кнопки Authorize в Swagger UI и в форму можно вставить токен.
}
