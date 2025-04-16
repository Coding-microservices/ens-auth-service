package io.vladprotchenko.authapi;

import feign.FeignException;
import io.vladprotchenko.authapi.config.FeignClientConfiguration;
import io.vladprotchenko.authapi.dto.response.AccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "auth-service",
        path = "api/v1",
        configuration = FeignClientConfiguration.class
)
public interface AuthServiceApi {

    /**
     * Retrieves a user profile by their accountId from the accounting-service.
     *
     * <p>This method calls the accounting-service endpoint to fetch a user's data by userId and returns it
     * as a {@link AccountDto}.
     * @param accountId the accountId of the user to retrieve.
     * @return a {@code ResponseEntity} containing the {@code AccountDto} with user details, or a {@code ResponseEntity} with an appropriate HTTP
     * status if the user is not found.
     * @throws FeignException if there is an issue with the communication with the auth-service.
     */
    @GetMapping("/account/{accountId}")
    ResponseEntity<?> getUserProfileById(@PathVariable("accountId") UUID accountId);

    /**
     * Retrieves a user profile by their authentication from the accounting-service.
     *
     * <p>This method calls the accounting-service endpoint to fetch a user's data by authentication and returns it
     * as a {@link AccountDto}.
     * @return a {@code ResponseEntity} containing the {@code AccountDto} with user details, or a {@code ResponseEntity} with an appropriate HTTP
     * status if the user is not found.
     * @throws FeignException if there is an issue with the communication with the auth-service.
     */
    @GetMapping("/account")
    ResponseEntity<?> getUserProfileByAuthentication();

}
