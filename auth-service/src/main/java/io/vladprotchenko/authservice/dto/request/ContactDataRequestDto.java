package io.vladprotchenko.authservice.dto.request;

import io.vladprotchenko.authservice.model.constant.ContactChannel;

import java.util.Map;

public record ContactDataRequestDto(
        Map<ContactChannel, String> contactData
) {
}
