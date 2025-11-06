package com.vacancy.model.dto;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vacancy.model.dto.in.OrganizationDtoIn;
import com.vacancy.model.dto.in.UserDtoIn;
import com.vacancy.model.dto.in.VacancyDtoIn;
import com.vacancy.model.dto.out.OrganizationDtoOut;
import com.vacancy.model.dto.out.UserDtoOut;
import com.vacancy.model.dto.out.UserVacancyResponseDtoOut;
import com.vacancy.model.dto.out.VacancyDtoOut;
import com.vacancy.model.entities.Organization;
import com.vacancy.model.entities.User;
import com.vacancy.model.entities.UserVacancyResponse;
import com.vacancy.model.entities.Vacancy;

@Configuration
public class MappersConfig {
    
    @Bean
    public ModelMapper organizationMapper() {
        ModelMapper mm = new ModelMapper();

        // user
        mm.createTypeMap(UserDtoIn.class, User.class);
        mm.createTypeMap(User.class, UserDtoOut.class);
        
        // organization
        mm.createTypeMap(OrganizationDtoIn.class, Organization.class);
        mm.createTypeMap(Organization.class, OrganizationDtoOut.class);
        
        // vacancy
        mm.createTypeMap(VacancyDtoIn.class, Vacancy.class);
        mm.createTypeMap(Vacancy.class, VacancyDtoOut.class);

        // user-vacancy response
        mm.createTypeMap(UserVacancyResponse.class, UserVacancyResponseDtoOut.class);

        return mm;
    }
}
