package io.vladprotchenko.authservice.repository;

import io.vladprotchenko.authservice.dto.response.UserListViewDto;
import io.vladprotchenko.authservice.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String email);

    Optional<Account> findByAccountId(UUID accountId);

    @Query("""
            select new io.vladprotchenko.authservice.dto.response.UserListViewDto(
            a.accountId, a.email, a.firstName, a.lastName, a.role.name)
            FROM Account a
            WHERE
                (:searchText IS NULL OR
                    lower(a.phoneNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                    LOWER(a.firstName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                    LOWER(a.lastName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
                    LOWER(a.email) LIKE LOWER(CONCAT('%', :searchText, '%'))
                )
                AND (
                (:admins IS TRUE AND a.role.name = 'ADMIN') OR
                (:clients IS TRUE AND a.role.name = 'USER')
               )
               AND (
               (:blocked is true and a.blockedUntil > :utcNow ) or
               (:blocked is false and (a.blockedUntil is null or a.blockedUntil <= :utcNow))
               )
               AND (:deleted = a.isSoftDeleted)
        """)
//    TODO: fix
    Page<UserListViewDto> searchUsers(
        @Param("searchText") String searchText,
        @Param("admins") boolean admins,
        @Param("users") boolean users,
        @Param("blocked") boolean blocked,
        @Param("deleted") boolean deleted,
        @Param("utcNow") Instant utcNow,
        Pageable pageable
    );
}
