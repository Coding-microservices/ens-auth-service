package io.vladprotchenko.authservice.service;

import io.vladprotchenko.authservice.dto.request.CreateUserRequest;
import io.vladprotchenko.authservice.dto.request.RegistrationRequest;
import io.vladprotchenko.authservice.dto.request.SocialUserInfo;
import io.vladprotchenko.authservice.dto.response.CreateUserResponse;
import io.vladprotchenko.authservice.dto.response.CreatedAccountDto;
import io.vladprotchenko.authservice.model.Account;
import io.vladprotchenko.authservice.model.Role;
import io.vladprotchenko.authservice.model.User;
import io.vladprotchenko.authservice.repository.RoleRepository;
import io.vladprotchenko.authservice.repository.UserRepository;
import io.vladprotchenko.ensstartercore.security.model.constant.UserRole;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    static final String LOG_USER_REGISTERED_SUCCESSFULLY = "User: {} registered successfully";

    UserRepository userRepository;
    RoleRepository roleRepository;
    AccountService accountService;

    @Transactional
    public CreateUserResponse createClient(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.email());

        CreatedAccountDto accountDto = createAndRegisterUser(request.email(), null, request.firstName(), request.lastName(), request.phoneNumber());

        String message = String.format(
            "User with email: %s created successfully",
            accountDto.getAccount().getEmail()
        );
        return new CreateUserResponse(message, accountDto.getPassword(), accountDto.getTempPasswordExpirationHours());
    }

//    @Transactional
//    public void register(RegistrationRequest request) {
//        log.debug("Registering user with email: {}", request.email());
//        createAndRegisterUser(request.email(), request.password(), request.firstName(), );
//    }

    private CreatedAccountDto createAndRegisterUser(String email, String password, String firstName, String lastName, String phoneNumber) {
        accountService.validateEmailNotRegistered(email);

        Role role = roleRepository.findByName(UserRole.USER).orElseThrow(
            () -> {
                log.error("Role with name: {} not found", UserRole.USER);
                return new EntityNotFoundException(
                    String.format("Role with name: %s not found", UserRole.USER));
            });

        CreatedAccountDto accountDto = accountService.createAccount(email, password, role, firstName, lastName, phoneNumber);

        Account account = accountDto.getAccount();

        save(account);

        log.info(LOG_USER_REGISTERED_SUCCESSFULLY, account.getEmail());
        return accountDto;
    }

    public Account createAndRegisterSocialUser(SocialUserInfo socialUserInfo) {
        String email = socialUserInfo.getEmail();
        log.info("Registering social user with email: {}", email);

        accountService.validateEmailNotRegistered(email);
        Role role = roleRepository.findByName(UserRole.USER).orElseThrow(
            () -> {
                log.error("Role with name: {} not found", UserRole.USER);
                return new EntityNotFoundException(
                    String.format("Role with name: %s not found", UserRole.USER));
            });

        var account = accountService.createAccountForSocialUser(socialUserInfo, role);

        save(account);

        log.info(LOG_USER_REGISTERED_SUCCESSFULLY, account.getEmail());
        return account;
    }

    public User findUserByAccountId(UUID accountId) {
        return userRepository.findByAccountId(accountId).orElseThrow(
            () -> {
                log.error("User with account: {} not found", accountId);
                return new EntityNotFoundException(
                    String.format("User with account: %s not found", accountId));
            });
    }

    private void save(Account account) {
        User user = new User()
            .setAccount(account);
        userRepository.save(user);
    }

}
