package io.vladprotchenko.authservice.repository;

import io.vladprotchenko.authservice.model.Role;
import io.vladprotchenko.ensstartercore.security.model.constant.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(UserRole name);
}
