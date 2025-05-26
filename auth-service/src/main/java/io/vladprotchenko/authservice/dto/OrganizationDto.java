package io.vladprotchenko.authservice.dto;

import io.vladprotchenko.authservice.model.Organization;

import java.time.Instant;

public record OrganizationDto(
        Long id,
        String name,
        Instant createdAt
) {
    public static OrganizationDto fromEntity(Organization org) {
        return new OrganizationDto(org.getId(), org.getName(), org.getCreatedAt());
    }
}
