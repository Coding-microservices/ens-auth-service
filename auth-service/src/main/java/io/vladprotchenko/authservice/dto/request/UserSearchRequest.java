package io.vladprotchenko.authservice.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class UserSearchRequest {
    String searchText;
    boolean admins;
    boolean users;
    boolean blocked;
    boolean deleted;
}
