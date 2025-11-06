package com.vacancy.controllers;

import com.vacancy.model.dto.in.OrganizationDtoIn;
import com.vacancy.model.dto.in.VacancyDtoIn;
import com.vacancy.model.dto.out.OrganizationDtoOut;
import com.vacancy.model.dto.out.UserVacancyResponseDtoOut;
import com.vacancy.model.dto.out.VacancyDtoOut;
import com.vacancy.model.entities.Organization;
import com.vacancy.model.entities.Vacancy;
import com.vacancy.service.OrganizationService;
import com.vacancy.service.UserVacancyResponseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final UserVacancyResponseService responseService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<OrganizationDtoOut>> getAllOrganizations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<Organization> organizationPage = organizationService.getAllOrganizations(page, size);
        List<OrganizationDtoOut> dtos = organizationPage.getContent().stream()
                .map(org -> modelMapper.map(org, OrganizationDtoOut.class)).toList();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(organizationPage.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationDtoOut> getOrganizationById(@PathVariable Long id) {
        Organization organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(modelMapper.map(organization, OrganizationDtoOut.class));
    }

    @PostMapping
    public ResponseEntity<OrganizationDtoOut> createOrganization(@Valid @RequestBody OrganizationDtoIn organizationDtoIn) {
        Organization organization = modelMapper.map(organizationDtoIn, Organization.class);
        Organization savedOrganization = organizationService.createOrganization(organization);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(savedOrganization, OrganizationDtoOut.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationDtoOut> updateOrganization(@PathVariable Long id, @Valid @RequestBody OrganizationDtoIn organizationDtoIn) {
        Organization organization = modelMapper.map(organizationDtoIn, Organization.class);
        Organization updatedOrganization = organizationService.updateOrganization(id, organization);
        return ResponseEntity.ok(modelMapper.map(updatedOrganization, OrganizationDtoOut.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orgId}/vacancies")
    public ResponseEntity<List<VacancyDtoOut>> getOrganizationVacancies(@PathVariable Long orgId) {
        List<Vacancy> vacancies = organizationService.getOrganizationVacancies(orgId);
        List<VacancyDtoOut> dtos = vacancies.stream()
                .map(vacancy -> modelMapper.map(vacancy, VacancyDtoOut.class)).toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{orgId}/vacancies")
    public ResponseEntity<VacancyDtoOut> publishVacancy(
            @PathVariable Long orgId,
            @Valid @RequestBody VacancyDtoIn vacancyDtoIn) {
        Vacancy vacancy = modelMapper.map(vacancyDtoIn, Vacancy.class);
        Vacancy savedVacancy = organizationService.publishVacancy(orgId, vacancy);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(savedVacancy, VacancyDtoOut.class));
    }

    @PutMapping("/{orgId}/vacancies/{vacancyId}")
    public ResponseEntity<VacancyDtoOut> updateOrganizationVacancy(
            @PathVariable Long orgId,
            @PathVariable Long vacancyId,
            @Valid @RequestBody VacancyDtoIn vacancyDtoIn) {
        Vacancy vacancy = modelMapper.map(vacancyDtoIn, Vacancy.class);
        Vacancy updatedVacancy = organizationService.updateOrganizationVacancy(orgId, vacancyId, vacancy);
        return ResponseEntity.ok(modelMapper.map(updatedVacancy, VacancyDtoOut.class));
    }

    @DeleteMapping("/{orgId}/vacancies/{vacancyId}")
    public ResponseEntity<Void> deleteOrganizationVacancy(
            @PathVariable Long orgId,
            @PathVariable Long vacancyId) {
        organizationService.deleteOrganizationVacancy(orgId, vacancyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orgId}/vacancies/{vacancyId}/responses")
    public ResponseEntity<List<UserVacancyResponseDtoOut>> getVacancyResponses(
            @PathVariable Long orgId,
            @PathVariable Long vacancyId) {
        organizationService.getOrganizationById(orgId);
        List<UserVacancyResponseDtoOut> responses = responseService.getVacancyResponses(vacancyId).stream()
                .map(response -> modelMapper.map(response, UserVacancyResponseDtoOut.class)).toList();
        return ResponseEntity.ok(responses);
    }
}
