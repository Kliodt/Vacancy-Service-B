package com.vacancy.vacancy.client;

import org.springframework.stereotype.Component;

@Component
public class OrganizationClientFallback implements OrganizationClient {

    @Override
    public Object getOrganizationById(Long id) {
        throw new RuntimeException("Сервис организаций недоступен");
    }

}
