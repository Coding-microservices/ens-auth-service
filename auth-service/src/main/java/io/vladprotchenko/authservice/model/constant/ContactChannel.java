package io.vladprotchenko.authservice.model.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContactChannel {
    EMAIL("email"),
    PHONE_NUMBER("phone_number"),
    PUSH("push");

    private final String prefix;
}
