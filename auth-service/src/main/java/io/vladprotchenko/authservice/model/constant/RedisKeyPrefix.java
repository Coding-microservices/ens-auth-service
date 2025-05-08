package io.vladprotchenko.authservice.model.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisKeyPrefix {
    LOGIN_OTP("login_otp:"),
    PASSWORD_RESET("password_reset_otp:"),
    EMAIL_CHANGE("email_change:");

    private final String prefix;
}
