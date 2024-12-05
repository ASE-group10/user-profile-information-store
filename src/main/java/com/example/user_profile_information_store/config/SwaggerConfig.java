package com.example.user_profile_information_store.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Profile Information Store API")
                        .version("1.0")
                        .description("API documentation for the User Profile Information Store application")
                        .contact(new Contact()
                                .name("ASE GROUP 10")));
    }
}
