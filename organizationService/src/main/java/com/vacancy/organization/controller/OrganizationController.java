package com.vacancy.organization.controller;

import com.vacancy.organization.model.Organization;
import com.vacancy.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;
    private static final String VACANCY_SERVICE_URL = "http://vacancy-service/api/vacancies";

    @GetMapping
    public ResponseEntity<List<Organization>> getAllOrganizations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<Organization> organizationPage = organizationService.getAllOrganizations(page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(organizationPage.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(organizationPage.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Organization> getOrganizationById(@PathVariable Long id) {
        Organization organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(organization);
    }

    @PostMapping
    public ResponseEntity<Organization> createOrganization(@Valid @RequestBody Organization organization) {
        Organization savedOrganization = organizationService.createOrganization(organization);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrganization);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable Long id, @Valid @RequestBody Organization organization) {
        Organization updatedOrganization = organizationService.updateOrganization(id, organization);
        return ResponseEntity.ok(updatedOrganization);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orgId}/vacancies")
    public ResponseEntity<List<Object>> getOrganizationVacancies(@PathVariable Long orgId) {
        Set<Long> vacancyIds = organizationService.getOrganizationVacancyIds(orgId);
        List<Object> vacancies = vacancyIds.stream()
                .map(id -> restTemplate.getForObject(VACANCY_SERVICE_URL + "/" + id, Object.class))
                .toList();
        return ResponseEntity.ok(vacancies);
    }

    @PostMapping("/{orgId}/vacancies/{vacancyId}")
    public ResponseEntity<Void> addVacancyToOrganization(
            @PathVariable Long orgId,
            @PathVariable Long vacancyId) {
        organizationService.addVacancyToOrganization(orgId, vacancyId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{orgId}/vacancies/{vacancyId}")
    public ResponseEntity<Void> updateOrganizationVacancy(
            @PathVariable Long orgId,
            @PathVariable Long vacancyId) {
        organizationService.updateOrganizationVacancy(orgId, vacancyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{orgId}/vacancies/{vacancyId}")
    public ResponseEntity<Void> deleteOrganizationVacancy(
            @PathVariable Long orgId,
            @PathVariable Long vacancyId) {
        organizationService.deleteOrganizationVacancy(orgId, vacancyId);
        return ResponseEntity.noContent().build();
    }
}
