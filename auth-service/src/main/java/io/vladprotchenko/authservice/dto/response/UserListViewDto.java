package io.vladprotchenko.authservice.dto.response;

import io.vladprotchenko.ensstartercore.security.model.constant.UserRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserListViewDto {
    UUID accountId;
    String email;
    String firstName;
    String lastName;
    UserRole role;
}