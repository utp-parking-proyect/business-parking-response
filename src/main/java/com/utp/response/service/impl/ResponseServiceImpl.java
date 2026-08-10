package com.utp.response.service.impl;

import com.utp.response.client.portal.PortalServiceClient;
import com.utp.response.generated.client.users.model.UserResponse;
import com.utp.response.generated.model.ParkingResponseIn;
import com.utp.response.model.entity.Request;
import com.utp.response.repository.RequestRepository;
import com.utp.response.repository.WorkflowRepository;
import com.utp.response.service.ResponseService;
import com.utp.response.util.Constants;
import com.utp.response.util.error.ConflictException;
import com.utp.response.util.error.ForbiddenException;
import com.utp.response.util.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResponseServiceImpl implements ResponseService {

  private final RequestRepository requestRepository;
  private final WorkflowRepository workflowRepository;
  private final PortalServiceClient portalServiceClient;
  private final TransactionalOperator transactionalOperator;

  @Override
  public Mono<Request> respondToRequest(Long authenticatedUserId, Integer requestId,
      ParkingResponseIn parkingResponseIn) {
    return portalServiceClient.getUserById(authenticatedUserId)
        .switchIfEmpty(Mono.error(new ForbiddenException(Constants.ERROR_USER_NOT_SAE)))
        .flatMap(this::validateSaeRole)
        .then(Mono.defer(() -> respondToExistingRequest(authenticatedUserId, requestId, parkingResponseIn)
            .as(transactionalOperator::transactional)));
  }

  private Mono<Request> respondToExistingRequest(Long authenticatedUserId, Integer requestId,
      ParkingResponseIn parkingResponseIn) {
    return requestRepository.findById(requestId)
        .switchIfEmpty(Mono.error(new NotFoundException(Constants.ERROR_REQUEST_NOT_FOUND)))
        .flatMap(request -> validateAcceptor(request, authenticatedUserId)
            .then(Mono.defer(() -> validateInReview(request)))
            .then(Mono.defer(() -> validateBody(parkingResponseIn)))
            .then(Mono.defer(() -> applyResponse(request, parkingResponseIn))));
  }

  private Mono<Void> validateSaeRole(UserResponse user) {
    return portalServiceClient.hasSaeRole(user)
        ? Mono.empty()
        : Mono.error(new ForbiddenException(Constants.ERROR_USER_NOT_SAE));
  }

  private Mono<Void> validateAcceptor(Request request, Long authenticatedUserId) {
    Integer userId = authenticatedUserId.intValue();
    return userId.equals(request.getIdAcceptor())
        ? Mono.empty()
        : Mono.error(new ForbiddenException(Constants.ERROR_NOT_ACCEPTOR));
  }

  private Mono<Void> validateInReview(Request request) {
    return Constants.ID_STATUS_IN_REVISION.equals(request.getIdStatus())
        ? Mono.empty()
        : Mono.error(new ConflictException(Constants.ERROR_REQUEST_NOT_IN_REVIEW));
  }

  private Mono<Void> validateBody(ParkingResponseIn parkingResponseIn) {
    if (parkingResponseIn.getApproved() == null) {
      return Mono.error(new IllegalArgumentException(Constants.ERROR_APPROVED_REQUIRED));
    }
    if (!parkingResponseIn.getApproved()
        && (parkingResponseIn.getComment() == null || parkingResponseIn.getComment().isBlank())) {
      return Mono.error(new IllegalArgumentException(Constants.ERROR_COMMENT_REQUIRED_ON_REJECTION));
    }
    return Mono.empty();
  }

  private Mono<Request> applyResponse(Request request, ParkingResponseIn parkingResponseIn) {
    Integer newStatus = Boolean.TRUE.equals(parkingResponseIn.getApproved())
        ? Constants.ID_STATUS_APPROVED
        : Constants.ID_STATUS_REJECTED;
    LocalDateTime now = LocalDateTime.now();

    return requestRepository
        .updateResponse(request.getIdRequest(), newStatus, now)
        .then(workflowRepository.saveWorkflow(request.getIdRequest(), newStatus, now,
            resolveObservation(parkingResponseIn)))
        .then(Mono.defer(() -> requestRepository.findById(request.getIdRequest())));
  }

  private String resolveObservation(ParkingResponseIn parkingResponseIn) {
    if (parkingResponseIn.getComment() != null && !parkingResponseIn.getComment().isBlank()) {
      return parkingResponseIn.getComment();
    }
    return Boolean.TRUE.equals(parkingResponseIn.getApproved())
        ? Constants.OBSERVATION_APPROVED_DEFAULT
        : Constants.OBSERVATION_REJECTED_DEFAULT;
  }
}
