package io.vladprotchenko.authservice.service;

import io.vladprotchenko.authservice.dto.OrganizationDto;
import io.vladprotchenko.authservice.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public OrganizationDto getById(Long id) {
        return organizationRepository.findById(id)
                .map(OrganizationDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with id: " + id));
    }

    public OrganizationDto getByName(String name) {
        return organizationRepository.findByName(name)
                .map(OrganizationDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with name: " + name));
    }

    public List<OrganizationDto> getAll() {
        return organizationRepository.findAll().stream()
                .map(OrganizationDto::fromEntity)
                .toList();
    }
}

