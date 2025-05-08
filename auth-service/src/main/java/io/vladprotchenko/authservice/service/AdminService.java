package io.vladprotchenko.authservice.service;

import io.vladprotchenko.authservice.dto.request.CreateAdminRequest;
import io.vladprotchenko.authservice.dto.response.CreateUserResponse;
import io.vladprotchenko.authservice.dto.response.CreatedAccountDto;
import io.vladprotchenko.authservice.model.Account;
import io.vladprotchenko.authservice.model.Admin;
import io.vladprotchenko.authservice.model.Role;
import io.vladprotchenko.authservice.repository.AdminRepository;
import io.vladprotchenko.authservice.repository.RoleRepository;
import io.vladprotchenko.ensstartercore.exception.custom.ForbiddenActionException;
import io.vladprotchenko.ensstartercore.security.dto.AdminDetailsDto;
import io.vladprotchenko.ensstartercore.security.model.constant.UserRole;
import io.vladprotchenko.ensstartercore.security.service.AuthenticationFacade;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    AdminRepository adminRepository;
    RoleRepository roleRepository;
    AccountService accountService;
    AuthenticationFacade authenticationFacade;

    @Transactional
    public CreateUserResponse createAdmin(CreateAdminRequest request) {

        String email = request.getEmail();
        log.info("Creating admin with email: {}", email);

        AdminDetailsDto adminDetails = authenticationFacade.getAdminDetailsFromAuthentication();
        boolean isCreatedSuperAdmin = request.isSuper();
        boolean isCreatingSuperAdmin = adminDetails.isSuperAdmin();

        if (isCreatedSuperAdmin && !isCreatingSuperAdmin) {
            log.error("Attempt to create super admin user with role admin by admin with account ID: {}", adminDetails.getAccountId());
            throw new ForbiddenActionException("Only Super Admin can create another Admin.");
        }

        accountService.validateEmailNotRegistered(email);

        Role role = roleRepository.findByName(UserRole.ADMIN).orElseThrow(
            () -> {
                log.error("Role with name: {} not found", UserRole.ADMIN);
                return new EntityNotFoundException(
                    String.format("Role with name: %s not found", UserRole.ADMIN));
            });

        CreatedAccountDto accountDto = accountService.createAccount(email, null, role, null);
        Account account = accountDto.getAccount();

        Admin admin = new Admin().setSuperAdmin(isCreatedSuperAdmin).setAccount(account);
        adminRepository.save(admin);

        log.info("Admin: {} with privilege isSuperAdmin = {} created successfully", email, isCreatedSuperAdmin);

        String
            message =
            String.format("Admin: %s with privilege isSuperAdmin = %s created successfully", email, isCreatedSuperAdmin);

        return new CreateUserResponse(message, accountDto.getPassword(), accountDto.getTempPasswordExpirationHours());
    }

    @Transactional
    public CreateUserResponse createSuperAdminOnStartup(String email) {
        log.info("Creating admin with email: {}", email);
        accountService.validateEmailNotRegistered(email);

        Role role = roleRepository.findByName(UserRole.ADMIN).orElseThrow(
            () -> {
                log.error("Role with name: {} not found", UserRole.ADMIN);
                return new EntityNotFoundException(
                    String.format("Role with name: %s not found", UserRole.ADMIN));
            });

        CreatedAccountDto accountDto = accountService.createAccount(email, null, role, null);
        Account account = accountDto.getAccount();

        Admin admin = new Admin().setSuperAdmin(true).setAccount(account);
        adminRepository.save(admin);

        log.info("Admin: {} with privilege isSuperAdmin = {} created successfully", email, true);

        String
            message =
            String.format("Admin: %s with privilege isSuperAdmin = %s created successfully", email, true);

        return new CreateUserResponse(message, accountDto.getPassword(), accountDto.getTempPasswordExpirationHours());
    }
}
