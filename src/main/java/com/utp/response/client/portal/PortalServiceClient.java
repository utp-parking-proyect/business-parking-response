package com.utp.response.client.portal;

import com.utp.response.generated.client.users.model.Role;
import com.utp.response.generated.client.users.model.UserResponse;
import com.utp.response.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PortalServiceClient {

  private final WebClient portalWebClient;

  public Mono<UserResponse> getUserById(Long id) {
    return portalWebClient.get()
        .uri("/users/{id}", id)
        .retrieve()
        .bodyToMono(UserResponse.class)
        .timeout(Constants.PORTAL_CLIENT_TIMEOUT);
  }

  public boolean hasSaeRole(UserResponse user) {
    if (user.getRoles() == null) {
      return false;
    }
    return user.getRoles().stream()
        .map(Role::getName)
        .anyMatch(Constants.ROLE_NAME_SAE::equalsIgnoreCase);
  }
}
