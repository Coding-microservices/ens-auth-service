package io.vladprotchenko.authservice.service;

import io.vladprotchenko.authapi.dto.response.AccountDto;
import io.vladprotchenko.authservice.dto.request.BlockUserRequest;
import io.vladprotchenko.authservice.dto.request.UnblockUserRequest;
import io.vladprotchenko.authservice.dto.request.UpdateAccountDto;
import io.vladprotchenko.authservice.dto.request.UserSearchRequest;
import io.vladprotchenko.authservice.dto.response.UserListViewDto;
import io.vladprotchenko.authservice.model.Account;
import io.vladprotchenko.authservice.repository.AccountRepository;
import io.vladprotchenko.authservice.util.UserMapper;
import io.vladprotchenko.ensstartercore.exception.custom.ForbiddenActionException;
import io.vladprotchenko.ensstartercore.model.kafka.UserDeletionEvent;
import io.vladprotchenko.ensstartercore.model.kafka.constant.KafkaConstants;
import io.vladprotchenko.ensstartercore.security.dto.AdminDetailsDto;
import io.vladprotchenko.ensstartercore.security.service.AuthenticationFacade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminAccountManagementService {

    @PersistenceContext
    EntityManager entityManager;

    UserMapper userMapper;
    AuthService authService;
    AccountService accountService;
    AccountRepository accountRepository;
    AuthenticationFacade authenticationFacade;
    KafkaTemplate<String, UserDeletionEvent> kafkaTemplate;

    @Transactional
    public AccountDto updateAccountByAdmin(UUID targetAccountId, UpdateAccountDto updateAccountDto) {
        var adminDetails = authenticationFacade.getAdminDetailsFromAuthentication();

        log.debug("Updating account with ID: {} by admin with ID: {}", targetAccountId, adminDetails.getAccountId());
        var targetAccount = accountService.findAccountByAccountId(targetAccountId);

        authService.validateUserModificationPermission(adminDetails, targetAccount);
        userMapper.updateAccountFromDto(updateAccountDto, targetAccount);

        accountRepository.save(targetAccount);
        log.debug("Account with ID: {} saved to db", targetAccountId);

        log.info("Account with ID: {} updated successfully", targetAccountId);

        return userMapper.userToClientDto(targetAccount);
    }

    public AccountDto getUserProfileById(UUID accountId) {

        //feature add specific data for each role

        var adminId = authenticationFacade.getAccountIdFromAuthentication();
        log.info("Getting account profile for user with ID: {} by Admin with ID: {}", accountId, adminId);

        var user = accountService.findAccountByAccountId(accountId);
        log.info("Successfully retrieved user profile for user with ID: {}", accountId);
        return userMapper.userToClientDto(user);
    }

    public Page<UserListViewDto> searchAccounts(UserSearchRequest request, int page, int size) {
        log.debug("Searching through accounts with parameters");
        Pageable pageable = PageRequest.of(page, size);

        entityManager.unwrap(Session.class).disableFilter(Account.FILTER_ACTIVE);

        Instant utcNow = Instant.now();

        return accountRepository.searchUsers(
            request.getSearchText(),
            request.isAdmins(),
            request.isUsers(),
            request.isBlocked(),
            request.isDeleted(),
            utcNow,
            pageable
        );
    }

    @Transactional
    public void blockAccount(BlockUserRequest request) {
        log.debug("Blocking user with account: {}", request.getAccountId());

        var targetAccount = accountService.findAccountByAccountId(request.getAccountId());
        AdminDetailsDto adminDetailsDto = authenticationFacade.getAdminDetailsFromAuthentication();

        validateSelfModification(targetAccount, "block");
        authService.validateUserModificationPermission(adminDetailsDto, targetAccount);

        targetAccount.setBlockedUntil(request.getExpiresAt());
        targetAccount.setRefreshToken(null);
        accountRepository.save(targetAccount);

        log.debug("User with account: {} blocked successfully", targetAccount.getAccountId());
    }

    @Transactional
    public void unblockAccount(UnblockUserRequest request) {
        log.debug("Unblocking user with ID: {}", request.getUserId());

        var targetAccount = accountService.findAccountByAccountId(request.getUserId());
        AdminDetailsDto adminDetailsDto = authenticationFacade.getAdminDetailsFromAuthentication();

        validateSelfModification(targetAccount, "unblock");
        authService.validateUserModificationPermission(adminDetailsDto, targetAccount);

        targetAccount.setBlockedUntil(null);
        accountRepository.save(targetAccount);

        log.debug("User with account: {} unblocked successfully", targetAccount.getAccountId());
    }

    @Transactional
    public void softDelete(UUID accountId) {
        log.debug("Deleting account: {} with soft delete", accountId);

        var targetAccount = accountService.findAccountByAccountId(accountId);
        AdminDetailsDto adminDetailsDto = authenticationFacade.getAdminDetailsFromAuthentication();

        validateSelfModification(targetAccount, "soft delete"); // feature convert to enum
        authService.validateUserModificationPermission(adminDetailsDto, targetAccount);

        targetAccount.setSoftDeleted(true);
        targetAccount.setRefreshToken(null);
        accountRepository.save(targetAccount);

        log.info("Account: {} deleted successfully with soft delete", accountId);
    }

    @Transactional
    public void hardDelete(UUID accountId) {
        log.debug("Deleting account: {} with hard delete", accountId);

        entityManager.unwrap(Session.class).disableFilter(Account.FILTER_ACTIVE);
        var account = accountRepository
            .findByAccountId(accountId)
            .orElseThrow(
                () -> {
                    log.error("Account with ID: {} not found", accountId);
                    return new EntityNotFoundException(
                        String.format("Account with ID: %s not found", accountId));
                });

        var adminDetails = authenticationFacade.getAdminDetailsFromAuthentication();
        validateSelfModification(account, "hard delete");
        authService.validateUserModificationPermission(adminDetails, account);

        accountRepository.delete(account);

        UserDeletionEvent event = new UserDeletionEvent(accountId, Instant.now());
        log.info("Start- Sending {} to Kafka Topic", KafkaConstants.USER_DELETION_EVENTS);
        kafkaTemplate.send(KafkaConstants.USER_DELETION_EVENTS, event);
        log.info("End- Sending {} to Kafka Topic ", KafkaConstants.USER_DELETION_EVENTS);

        log.debug("Account: {} deleted successfully with hard delete", accountId);
    }

    private void validateSelfModification(Account account, String action) {
        boolean isSelfModification = account.getAccountId().equals(authenticationFacade.getAccountIdFromAuthentication());
        if (isSelfModification) {
            log.error("Attempt to {} oneself. User with account: {}", action, account.getAccountId());
            throw new ForbiddenActionException(String.format("You can't %s yourself", action));
        }
    }
}
