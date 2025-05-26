package io.vladprotchenko.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vladprotchenko.authservice.dto.OrganizationDto;
import io.vladprotchenko.authservice.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization", description = "Endpoints for managing organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Get organization by ID",
            description = "Returns an organization by its unique identifier"
    )
    public ResponseEntity<OrganizationDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(organizationService.getById(id));
    }

    @GetMapping("/by-name")
    @Operation(
            summary = "Get organization by name",
            description = "Returns an organization by its exact name"
    )
    public ResponseEntity<OrganizationDto> getByName(@RequestParam String name) {
        return ResponseEntity.ok(organizationService.getByName(name));
    }

    @GetMapping
    @Operation(
            summary = "Get all organizations",
            description = "Returns a list of all organizations in the system"
    )
    public ResponseEntity<List<OrganizationDto>> getAll() {
        return ResponseEntity.ok(organizationService.getAll());
    }
}