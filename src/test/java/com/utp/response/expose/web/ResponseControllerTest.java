package com.utp.response.expose.web;

import com.utp.response.generated.model.ParkingResponseIn;
import com.utp.response.model.entity.Request;
import com.utp.response.service.ResponseService;
import com.utp.response.util.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponseControllerTest {

  @Mock
  private ResponseService responseService;

  @Mock
  private AuthenticatedUserProvider authenticatedUserProvider;

  @Mock
  private ServerWebExchange exchange;

  @InjectMocks
  private ResponseController controller;

  @Test
  void testRespondToParkingRequest_Approve_Success() {
    Request savedRequest = new Request();
    savedRequest.setIdRequest(10);
    savedRequest.setIdStatus(3);

    ParkingResponseIn body = new ParkingResponseIn().approved(true).comment("Solicitud aprobada correctamente.");

    when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(Mono.just(3L));
    when(responseService.respondToRequest(eq(3L), eq(10), any())).thenReturn(Mono.just(savedRequest));

    StepVerifier.create(controller.respondToParkingRequest(
            "550e8400-e29b-41d4-a716-446655440000",
            "2025-01-10T14:02:03.987-0500",
            "P0",
            "atlas-cross-services",
            10,
            Mono.just(body),
            exchange))
        .assertNext(response -> {
          assertEquals(HttpStatus.OK, response.getStatusCode());
          assertNotNull(response.getBody());
          assertEquals(true, response.getBody().getApproved());
          assertEquals(3, response.getBody().getStatusId());
          assertEquals("Solicitud aprobada correctamente.", response.getBody().getComment());
        })
        .verifyComplete();
  }

  @Test
  void testRespondToParkingRequest_Reject_Success() {
    Request savedRequest = new Request();
    savedRequest.setIdRequest(10);
    savedRequest.setIdStatus(4);

    ParkingResponseIn body = new ParkingResponseIn().approved(false)
        .comment("La documentación presentada no es válida.");

    when(authenticatedUserProvider.getAuthenticatedUserId()).thenReturn(Mono.just(3L));
    when(responseService.respondToRequest(eq(3L), eq(10), any())).thenReturn(Mono.just(savedRequest));

    StepVerifier.create(controller.respondToParkingRequest(
            "550e8400-e29b-41d4-a716-446655440000",
            "2025-01-10T14:02:03.987-0500",
            "P0",
            "atlas-cross-services",
            10,
            Mono.just(body),
            exchange))
        .assertNext(response -> {
          assertEquals(HttpStatus.OK, response.getStatusCode());
          assertNotNull(response.getBody());
          assertEquals(false, response.getBody().getApproved());
          assertEquals(4, response.getBody().getStatusId());
        })
        .verifyComplete();
  }
}
