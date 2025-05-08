package io.vladprotchenko.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class CreateAdminRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    String email;

    boolean isSuper;
}
