package com.vacancy.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

import feign.Contract;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import reactivefeign.spring.config.EnableReactiveFeignClients;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@SpringBootApplication
@EnableDiscoveryClient
@EnableReactiveFeignClients
@Configuration
@OpenAPIDefinition
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("http://localhost:8080")))
                .info(new Info().title("User service API").version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public Contract reactiveFeignContract(@Qualifier("webFluxConversionService") ConversionService conversionService) {
        // for feign
        return new SpringMvcContract(new ArrayList<>(), conversionService);
    }

    @Bean
    public CircuitBreaker configVacancyServiceCB(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .ignoreExceptions(FeignException.NotFound.class)
                .build();
        return registry.circuitBreaker("vacancy-service", config);
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
