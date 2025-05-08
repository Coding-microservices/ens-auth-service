package io.vladprotchenko.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateUserResponse {
    private String message;
    private String tempPassword;
    private Integer passwordExpirationH;
}
