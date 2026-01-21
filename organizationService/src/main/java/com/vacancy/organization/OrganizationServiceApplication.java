package com.vacancy.organization;

import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@SpringBootApplication
@EnableDiscoveryClient
@Configuration
@OpenAPIDefinition
public class OrganizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrganizationServiceApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("http://localhost:8080")))
                .info(new Info().title("Organization service API").version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }


    @Bean
    public KafkaSender<String, String> kafkaSender(KafkaProperties kafkaProperties) {
        // for reactive kafka
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        SenderOptions<String, String> options = SenderOptions.create(props);
        return KafkaSender.create(options);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
