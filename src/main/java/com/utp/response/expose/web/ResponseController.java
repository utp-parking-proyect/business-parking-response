package com.utp.response.expose.web;

import com.utp.response.generated.api.ResponseApi;
import com.utp.response.generated.model.ParkingResponseIn;
import com.utp.response.generated.model.ParkingResponseOut;
import com.utp.response.service.ResponseService;
import com.utp.response.util.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ResponseController implements ResponseApi {

  private final ResponseService responseService;
  private final AuthenticatedUserProvider authenticatedUserProvider;

  @Override
  public Mono<ResponseEntity<ParkingResponseOut>> respondToParkingRequest(
      String requestID,
      String requestDate,
      String appCode,
      String callerName,
      Integer requestId,
      Mono<ParkingResponseIn> parkingResponseIn,
      ServerWebExchange exchange) {

    return Mono.zip(authenticatedUserProvider.getAuthenticatedUserId(), parkingResponseIn)
        .flatMap(tuple -> responseService.respondToRequest(tuple.getT1(), requestId, tuple.getT2())
            .map(savedRequest -> {
              log.info("Parking request responded successfully - RequestId: {}", savedRequest.getIdRequest());
              return ResponseEntity.status(HttpStatus.OK)
                  .header("Request-ID", requestID)
                  .header("request-date", requestDate)
                  .header("app-code", appCode)
                  .header("caller-name", callerName)
                  .body(new ParkingResponseOut()
                      .approved(tuple.getT2().getApproved())
                      .comment(tuple.getT2().getComment())
                      .statusId(savedRequest.getIdStatus()));
            }));
  }
}
