package io.vladprotchenko.authservice.service;

import io.jsonwebtoken.Jwts;
import io.vladprotchenko.authservice.dto.response.TokensResponse;
import io.vladprotchenko.authservice.model.Account;
import io.vladprotchenko.authservice.model.Admin;
import io.vladprotchenko.authservice.model.RefreshToken;
import io.vladprotchenko.authservice.model.User;
import io.vladprotchenko.authservice.repository.AdminRepository;
import io.vladprotchenko.authservice.repository.UserRepository;
import io.vladprotchenko.ensstartercore.exception.EnsServiceException;
import io.vladprotchenko.ensstartercore.exception.custom.AuthorizationException;
import io.vladprotchenko.ensstartercore.exception.custom.UserBlockedException;
import io.vladprotchenko.ensstartercore.security.service.JwtTokenValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.vladprotchenko.ensstartercore.security.model.constant.JwtConstants.CLAIM_ACCOUNT_ID;
import static io.vladprotchenko.ensstartercore.security.model.constant.JwtConstants.CLAIM_FIRST_NAME;
import static io.vladprotchenko.ensstartercore.security.model.constant.JwtConstants.CLAIM_ROLE;
import static io.vladprotchenko.ensstartercore.security.model.constant.JwtConstants.IS_SUPER_ADMIN_CLAIM;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenIssuer {

    RefreshTokenService refreshTokenService;
    UserRepository userRepository;
    AdminRepository adminRepository;
    JwtTokenValidator jwtTokenValidator;

    @Value("${app.jwtExpirationS}") long jwtExpirationS;
    @Value("${app.refreshTokenExpirationS}") long refreshTokenExpirationS;

    public String generateAccessToken(Account account) {

        Map<String, Object> claims = new HashMap<>();

        var role = account.getRole().getName();
        UUID accountId = account.getAccountId();

        claims.put(CLAIM_FIRST_NAME, account.getFirstName());
        claims.put(CLAIM_ROLE, role);

        switch (role) {
            case USER:
                User user = userRepository.findByAccountId(accountId)
                    .orElseThrow(() -> {
                        log.error("Client for account ID: {} not found", account.getAccountId());
                        return new EntityNotFoundException("Client not found");
                    });
                break;

            case ADMIN:
                Admin admin = adminRepository.findByAccountId(accountId)
                    .orElseThrow(() -> {
                        log.error("Admin for account with ID: {} not found", account.getAccountId());
                        return new EntityNotFoundException("Admin not found");
                    });
                claims.put(IS_SUPER_ADMIN_CLAIM, admin.isSuperAdmin());
                break;

            default:
                break;
        }

        claims.put(CLAIM_ACCOUNT_ID, account.getAccountId());
        return Jwts.builder()
            .subject(account.getEmail())
            .claims(claims)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + Duration.ofSeconds(jwtExpirationS).toMillis()))
            .signWith(jwtTokenValidator.getSigningKey())
            .compact();
    }

    public TokensResponse refreshTokens(String refreshToken) {

        Account account;
        try {
            RefreshToken storedRefreshtoken = refreshTokenService.findByToken(refreshToken);
            refreshTokenService.verifyExpiration(storedRefreshtoken);

            account = storedRefreshtoken.getAccount();

        } catch (EnsServiceException e) {
            log.debug("Exception message: {}", e.getMessage());
            throw new AuthorizationException("Your session has expired. Please log in again.");
        }
        boolean isBlocked =
            account.getBlockedUntil() != null && account.getBlockedUntil().isAfter(Instant.now());
        if (isBlocked) {
            log.debug("Account with ID: {} is blocked", account.getAccountId());
            throw new UserBlockedException(
                String.format("Your account is blocked due to: %s", account.getBlockedUntil()));
        }

        String newAccessToken = generateAccessToken(account);
        log.debug("Access token refreshed successfully for account: {}", account.getEmail());

        RefreshToken newRefreshToken = createRefreshToken(account.getEmail());

        return new TokensResponse(
            newAccessToken, newRefreshToken.getToken(), jwtExpirationS, refreshTokenExpirationS);
    }

    public RefreshToken createRefreshToken(String email) {
        return refreshTokenService.createRefreshToken(email);
    }
}
