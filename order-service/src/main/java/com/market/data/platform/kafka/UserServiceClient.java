package com.market.data.platform.kafka;


import com.market.data.platform.dto.response.UserResponseDTO;
import com.market.data.platform.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.user-service.name:user-service}")
    private String userServiceName;

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    /**
     * Look up a user by ID.
     *
     * Returns Optional.empty() only when the user is genuinely not found (404).
     * All other HTTP errors are propagated as exceptions so the caller can decide
     * how to handle them.
     */
    public Optional<UserResponseDTO> getUserById(Long userId) {
        log.debug("Calling User Service for userId={}", userId);

        try {
            UserResponseDTO user = webClientBuilder
                    .build()
                    .get()
                    .uri("http://{service}/api/users/{id}", userServiceName, userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        if (response.statusCode().value() == 404) {
                            return Mono.error(new UserNotFoundException(userId));
                        }
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "User Service 4xx error for userId=" + userId + ": " + body)));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "User Service 5xx error for userId=" + userId + ": " + body))))
                    .bodyToMono(UserResponseDTO.class)
                    .timeout(TIMEOUT)
                    .block();

            log.debug("User Service returned userId={}", userId);
            return Optional.ofNullable(user);

        } catch (UserNotFoundException ex) {
            log.warn("User not found in User Service: userId={}", userId);
            return Optional.empty();

        } catch (Exception ex) {
            log.error("User Service call failed for userId={}: {}", userId, ex.getMessage(), ex);
            throw new RuntimeException("Unable to reach User Service for userId=" + userId, ex);
        }
    }

    /**
     * Lightweight existence check — avoids deserializing the full response body.
     */
    public boolean userExists(Long userId) {
        return getUserById(userId).isPresent();
    }
}