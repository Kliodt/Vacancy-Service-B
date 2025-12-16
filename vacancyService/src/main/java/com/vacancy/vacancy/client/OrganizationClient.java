package com.vacancy.vacancy.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "organization-service", path = "/api/organizations")
public interface OrganizationClient {

    @GetMapping("/{id}")
    Object getOrganizationById(@PathVariable("id") Long id);

}
