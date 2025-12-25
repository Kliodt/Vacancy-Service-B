package com.vacancy.organization.model.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class OrganizationResponseDto {
    private Long id;
    private String nickname;
    private String email;
    private Long director;
}
