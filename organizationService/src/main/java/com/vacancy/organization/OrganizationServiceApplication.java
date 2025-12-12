package com.vacancy.organization;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.CustomConversions.StoreConversions;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.vacancy.organization.utils.converters.JsonToListLongConverter;
import com.vacancy.organization.utils.converters.ListLongToJsonConverter;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@Configuration
@OpenAPIDefinition
public class OrganizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrganizationServiceApplication.class, args);
    }

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .servers(List.of(new Server().url("http://localhost:8080")))
                .info(new Info().title("Organization service API").version("1.0.0"));
    }

    @Bean
    public HttpMessageConverters messageConverters() {
        // for Feign
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new MappingJackson2HttpMessageConverter());
        return new HttpMessageConverters(converters);
    }

    @Bean
    @SuppressWarnings("null")
    public R2dbcCustomConversions r2dbcCustomConversions() {
        // for r2dbc
        List<Converter<?, ?>> converters = List.of(
                new JsonToListLongConverter(),
                new ListLongToJsonConverter());
        return new R2dbcCustomConversions(StoreConversions.NONE, converters);
    }

}
