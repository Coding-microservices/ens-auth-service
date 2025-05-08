package io.vladprotchenko.authservice.config;

import io.vladprotchenko.authservice.dto.response.CreateUserResponse;
import io.vladprotchenko.authservice.repository.AdminRepository;
import io.vladprotchenko.authservice.service.AdminService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminConfig {

    AdminRepository adminRepository;
    AdminService adminService;

    @EventListener(ApplicationReadyEvent.class)
    public void createSuperAdmin(ApplicationReadyEvent event) {
        if (adminRepository.findAll().isEmpty()) {
            CreateUserResponse response = adminService.createSuperAdminOnStartup("vlad.freezzy@gmail.com");
            log.info(response.toString());
        }

    }
}
