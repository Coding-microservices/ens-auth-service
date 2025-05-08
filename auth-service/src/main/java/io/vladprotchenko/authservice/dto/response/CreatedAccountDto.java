package io.vladprotchenko.authservice.dto.response;

import io.vladprotchenko.authservice.model.Account;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatedAccountDto {
    Account account;
    String password;
    int tempPasswordExpirationHours;
}
