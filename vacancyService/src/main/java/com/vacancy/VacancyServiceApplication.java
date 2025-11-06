package com.vacancy;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VacancyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VacancyServiceApplication.class, args);
    }

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info().title("Vacancy service API").version("1.0.0"));
    }
}
