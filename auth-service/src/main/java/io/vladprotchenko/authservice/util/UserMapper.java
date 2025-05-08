package io.vladprotchenko.authservice.util;

import io.vladprotchenko.authapi.dto.response.AccountDto;
import io.vladprotchenko.authservice.dto.request.UpdateAccountDto;
import io.vladprotchenko.authservice.model.Account;
import io.vladprotchenko.authservice.model.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role", qualifiedByName = "roleToUserRole")
    AccountDto userToClientDto(Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAccountFromDto(UpdateAccountDto dto, @MappingTarget Account account);

    @Named("roleToUserRole")
    static String roleToUserRole(Role role) {
        return role != null ? role.getName().name() : null;
    }
}
