package io.vladprotchenko.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vladprotchenko.authapi.dto.response.AccountDto;
import io.vladprotchenko.authservice.dto.request.ChangePasswordRequest;
import io.vladprotchenko.authservice.dto.request.PasswordResetWithOtpDto;
import io.vladprotchenko.authservice.dto.request.UpdateAccountDto;
import io.vladprotchenko.authservice.dto.response.AuthResponse;
import io.vladprotchenko.authservice.dto.response.ChangeEmailResponse;
import io.vladprotchenko.authservice.dto.response.SuccessResponse;
import io.vladprotchenko.authservice.service.AccountService;
import io.vladprotchenko.ensstartercore.security.model.UserDetailsImpl;
import io.vladprotchenko.ensstartercore.security.service.AuthenticationFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Account", description = "User account management")
@SecurityRequirement(name = "BearerAuth")
@Validated
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final AccountService userService;
    private final AccountService accountService;
    private final AuthenticationFacade authenticationFacade;

    @Operation(summary = "Update user", description = "Allows updating user information.")
    @PatchMapping
    public ResponseEntity<AccountDto> updateUser(@RequestBody @Valid UpdateAccountDto updateAccountDto) {
        var updatedUser = userService.updateUserByAuthentication(updateAccountDto);
        return ResponseEntity.ok().body(updatedUser);
    }

    @Operation(
        summary = "Get current user profile",
        description = "Retrieve detailed user information by authentication.")
    @GetMapping()
    public ResponseEntity<AccountDto> getUserByAuth() {
        return ResponseEntity.ok(userService.getUserProfileByAuthentication());
    }

    @Operation(summary = "Change password")
    @PatchMapping("/password")
    public ResponseEntity<SuccessResponse> changePassword(@RequestBody @Valid ChangePasswordRequest request) {

        String userEmail =
            ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getEmail();
        log.info("Attempt to change password for user: {}", userEmail);
        AuthResponse response = userService.changePassword(request, userEmail);

        log.info("Logout successful for user: {}", userEmail);

        return ResponseEntity.ok().body(new SuccessResponse(response.getMessage(), response.getLink()));
    }

    @PostMapping("/email")
    @Operation(summary = "Change email")
    public ResponseEntity<ChangeEmailResponse> changeEmailRequest(
        @RequestParam("newEmail")
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email cannot be empty")
        String newEmail) {

        var userId = authenticationFacade.getAccountIdFromAuthentication();
        log.info("Request to change email for user with ID: {}", userId);
        ChangeEmailResponse response = userService.changeEmailRequest(newEmail);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/email-confirm")
    @Operation(summary = "Change email")
    public ResponseEntity<ChangeEmailResponse> changeEmailConfirm(
        @RequestParam("newEmail")
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email cannot be empty")
        String newEmail,
        @RequestParam(name = "code")
        @Pattern(
            regexp = "^\\d{6}$",
            message = "One-time password must consist of exactly 6 digits")
        String code) {
        var userId = authenticationFacade.getAccountIdFromAuthentication();
        log.info("Request to confirm changing email for user with ID: {}", userId);
        ChangeEmailResponse response = userService.changeEmailConfirm(newEmail, code);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Request password reset")
    @PostMapping("/password/reset")
    public ResponseEntity<SuccessResponse> initiatePasswordReset(
        @RequestParam("email")
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email cannot be empty")
        String email) {

        log.info("Reset password attempt for user: {}", email);
        SuccessResponse response = accountService.passwordResetRequest(email);
        log.info("Email with otp to reset password was sent to the email: {}", email);

        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Reset password with OTP")
    @PostMapping("/password/reset-confirm")
    public ResponseEntity<SuccessResponse> resetPasswordWithOtp(
        @RequestBody @Valid PasswordResetWithOtpDto request) {

        log.info("Reset password with otp attempt for email: {}", request.getEmail());
        SuccessResponse response = accountService.passwordResetConfirm(request);

        return ResponseEntity.ok().body(response);
    }
}
