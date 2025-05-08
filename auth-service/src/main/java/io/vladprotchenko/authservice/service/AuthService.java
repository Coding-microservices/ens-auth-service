package io.vladprotchenko.authservice.service;

import io.vladprotchenko.authservice.dto.request.LoginRequest;
import io.vladprotchenko.authservice.dto.request.VerifyLoginOtpRequest;
import io.vladprotchenko.authservice.dto.response.AuthResponse;
import io.vladprotchenko.authservice.dto.response.TokensResponse;
import io.vladprotchenko.authservice.model.Account;
import io.vladprotchenko.authservice.model.RefreshToken;
import io.vladprotchenko.authservice.model.constant.RedisKeyPrefix;
import io.vladprotchenko.authservice.repository.AccountRepository;
import io.vladprotchenko.authservice.repository.RefreshTokenRepository;
import io.vladprotchenko.ensstartercore.exception.custom.AuthorizationException;
import io.vladprotchenko.ensstartercore.exception.custom.ForbiddenActionException;
import io.vladprotchenko.ensstartercore.security.dto.AdminDetailsDto;
import io.vladprotchenko.ensstartercore.security.model.constant.UserRole;
import io.vladprotchenko.ensstartercore.security.service.AuthenticationFacade;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    static final String LOG_ACCOUNT_NOT_FOUND_EMAIL = "Account with email: {} not found";
    static final String LOG_ACCOUNT_NOT_FOUND_ID = "Account with ID: {} not found";
    static final String LOG_REFRESH_TOKEN_DELETED = "Refresh token deleted successfully for user: {}";
    static final String LOG_SENDING_EMAIL_ERROR = "Error during sending 2FA verification email for: {}. StackTrace: {}";
    static final String LOG_PASSWORD_HAS_BEEN_RESET = "Password has been reset for user: {} by admin: {}";

    static final String PASSWORD_RESET = "Password reset";
    static final String VERIFICATION_CODE_2FA = "2FA Verification Code";

    CustomAuthenticationProvider authenticationProvider;
    JwtTokenIssuer jwtTokenIssuer;
    AuthenticationFacade authenticationFacade;
    RefreshTokenRepository refreshTokenRepository;
    AccountRepository accountRepository;
    RedisService redisService;

    @Value("${app.jwtExpirationS}")
    Long jwtExpirationS;
    @Value("${app.otp.loginExpirationMinutes}")
    int loginOtpExpirationMinutes;
    @Value("${app.tempPassword.expirationHours}")
    int tempPasswordExpirationHours;
    @Value("${app.refreshTokenExpirationS}")
    Long refreshTokenExpirationS;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        String accountEmail = loginRequest.getEmail();
        log.debug("Authenticating user: {}", accountEmail);

        Authentication authentication = createAuthentication(accountEmail, loginRequest.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Account account = (Account) authentication.getPrincipal();

        log.debug("User with account: {} successfully authenticated.", account.getAccountId());

//        TODO: maybe add 2FA

        return buildAuthResponse(account);
    }

    private AuthResponse buildAuthResponse(Account account) {
        String newAccessToken = jwtTokenIssuer.generateAccessToken(account);
        log.debug("Access token generated successfully for account: {}", account.getEmail());

        RefreshToken newRefreshToken = jwtTokenIssuer.createRefreshToken(account.getEmail());

        return AuthResponse.builder()
                .tokens(new TokensResponse(newAccessToken, newRefreshToken.getToken(), jwtExpirationS, refreshTokenExpirationS))
                .build();
    }

    public Authentication createAuthentication(String accountEmail, String password) {
        return authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(accountEmail, password));
    }

    @Transactional
    public AuthResponse logoutUser() {
        String userEmail = authenticationFacade.getUserEmailFromAuthentication();
        log.info("Logout attempt for user: {}", userEmail);
        refreshTokenRepository.deleteRefreshTokenByAccountEmail(userEmail);
        log.debug(LOG_REFRESH_TOKEN_DELETED, userEmail);

        log.info("Logout successful for user: {}", userEmail);
        return AuthResponse.builder().message(String.format("Logout successful for user %s", userEmail)).build();
    }

    @Transactional
    public AuthResponse completeLoginWithOtp(VerifyLoginOtpRequest request) {
        String accountEmail = request.getEmail();
        log.debug("Verifying otp to log in for email: {}", accountEmail);

        String otpFromRequest = request.getOtp();
        String redisKey = RedisKeyPrefix.LOGIN_OTP.getPrefix() + accountEmail;
        String storedOtp = redisService.getOtp(redisKey);

        if (!otpFromRequest.equals(storedOtp)) {
            log.debug("OTP has expired or is invalid for email: {}", accountEmail);
            log.info("Failed to login for user: {}", accountEmail);
            throw new InvalidRequestException("OTP has expired or is invalid. Please try again.");
        }

        redisService.deleteOtp(redisKey);

        var account = accountRepository.findByEmail(accountEmail).orElseThrow(() -> {
            log.debug(LOG_ACCOUNT_NOT_FOUND_EMAIL, accountEmail);
            return new AuthorizationException("Failed to verify otp. Please try again.");
        });

        return buildAuthResponse(account);
    }

    public void validateUserModificationPermission(AdminDetailsDto changer, Account targetAccount) {

        UUID editorAccountId = changer.getAccountId();
        UUID targetAccountId = targetAccount.getAccountId();
        UserRole targetRole = targetAccount.getRole().getName();

        log.debug("Validating user modification permission. Editor ID: {}, Target ID: {}, Target Role: {}",
            editorAccountId, targetAccountId, targetRole);

        if (changer.isSuperAdmin()) {
            log.debug("Editor with ID: {} is a super admin. Modification allowed.", editorAccountId);
            return;
        }

        boolean
            isTargetUserAllowed =
            targetAccount.getRole().getName().equals(UserRole.USER);

        if (!isTargetUserAllowed) {
            log.error("Permission denied. Editor ID: {}, Target ID: {}, Target Role: {}",
                editorAccountId, targetAccountId, targetRole);
            throw new ForbiddenActionException("You can't edit this user");
        }
        log.debug("User modification validation passed. Editor ID: {}, Target ID: {}", editorAccountId, targetAccountId);
    }
}
