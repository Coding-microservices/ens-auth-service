package io.vladprotchenko.authservice.repository;


import io.vladprotchenko.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
            SELECT u FROM User u
            JOIN Account a ON u.account.id = a.id
            WHERE a.accountId = :accountId
        """)
    Optional<User> findByAccountId(@Param("accountId") UUID accountId);
}
