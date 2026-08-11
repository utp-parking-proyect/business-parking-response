package com.utp.response.service.impl;

import com.utp.response.client.portal.PortalServiceClient;
import com.utp.response.generated.client.users.model.Role;
import com.utp.response.generated.client.users.model.UserResponse;
import com.utp.response.generated.model.ParkingResponseIn;
import com.utp.response.model.entity.Request;
import com.utp.response.repository.RequestRepository;
import com.utp.response.repository.WorkflowRepository;
import com.utp.response.util.Constants;
import com.utp.response.util.error.ConflictException;
import com.utp.response.util.error.ForbiddenException;
import com.utp.response.util.error.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponseServiceImplTest {

  private static final Long ACCEPTOR_ID = 3L;
  private static final Integer REQUEST_ID = 10;

  @Mock
  private RequestRepository requestRepository;
  @Mock
  private WorkflowRepository workflowRepository;
  @Mock
  private PortalServiceClient portalServiceClient;
  @Mock
  private TransactionalOperator transactionalOperator;

  @InjectMocks
  private ResponseServiceImpl responseService;

  @BeforeEach
  void mockTransactionalOperator() {
    Mockito.lenient().when(transactionalOperator.transactional(any(Mono.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Mono.class));
  }

  private UserResponse saeUser(Long idUser) {
    Role role = new Role();
    role.setIdRole(1L);
    role.setName("ROLE_SAE");
    UserResponse user = new UserResponse();
    user.setIdUser(idUser);
    user.setRoles(List.of(role));
    return user;
  }

  private UserResponse nonSaeUser(Long idUser) {
    UserResponse user = new UserResponse();
    user.setIdUser(idUser);
    user.setRoles(List.of());
    return user;
  }

  private Request requestInReview() {
    Request request = new Request();
    request.setIdRequest(REQUEST_ID);
    request.setIdAcceptor(3);
    request.setIdStatus(Constants.ID_STATUS_IN_REVISION);
    return request;
  }

  @Test
  void testRespondToRequest_Approve_Success() {
    when(portalServiceClient.getUserById(ACCEPTOR_ID)).thenReturn(Mono.just(saeUser(ACCEPTOR_ID)));
    when(portalServiceClient.hasSaeRole(any())).thenReturn(true);
    when(requestRepository.updateResponse(eq(REQUEST_ID), eq(Constants.ID_STATUS_APPROVED), any()))
        .thenReturn(Mono.empty());
    when(workflowRepository.saveWorkflow(eq(REQUEST_ID), eq(Constants.ID_STATUS_APPROVED), any(), any()))
        .thenReturn(Mono.empty());

    Request updated = requestInReview();
    updated.setIdStatus(Constants.ID_STATUS_APPROVED);
    when(requestRepository.findById(REQUEST_ID)).thenReturn(Mono.just(requestInReview()), Mono.just(updated));

    ParkingResponseIn body = new ParkingResponseIn().approved(true).comment("Solicitud aprobada correctamente.");

    StepVerifier.create(responseService.respondToRequest(ACCEPTOR_ID, REQUEST_ID, body))
        .assertNext(saved -> assertEquals(Constants.ID_STATUS_APPROVED, saved.getIdStatus()))
        .verifyComplete();
  }

  @Test
  void testRespondToRequest_Reject_Success() {
    when(portalServiceClient.getUserById(ACCEPTOR_ID)).thenReturn(Mono.just(saeUser(ACCEPTOR_ID)));
    when(portalServiceClient.hasSaeRole(any())).thenReturn(true);

    Request updated = requestInReview();
    updated.setIdStatus(Constants.ID_STATUS_REJECTED);
    when(requestRepository.findById(REQUEST_ID)).thenReturn(Mono.just(requestInReview()), Mono.just(updated));
    when(requestRepository.updateResponse(eq(REQUEST_ID), eq(Constants.ID_STATUS_REJECTED), any()))
        .thenReturn(Mono.empty());
    when(workflowRepository.saveWorkflow(eq(REQUEST_ID), eq(Constants.ID_STATUS_REJECTED), any(), any()))
        .thenReturn(Mono.empty());

    ParkingResponseIn body = new ParkingResponseIn().approved(false)
        .comment("La documentación presentada no es válida.");

    StepVerifier.create(responseService.respondToRequest(ACCEPTOR_ID, REQUEST_ID, body))
        .assertNext(saved -> assertEquals(Constants.ID_STATUS_REJECTED, saved.getIdStatus()))
        .verifyComplete();
  }

  @Test
  void testRespondToRequest_UserWithoutSaeRole_IsForbidden() {
    when(portalServiceClient.getUserById(ACCEPTOR_ID)).thenReturn(Mono.just(nonSaeUser(ACCEPTOR_ID)));
    when(portalServiceClient.hasSaeRole(any())).thenReturn(false);

    ParkingResponseIn body = new ParkingResponseIn().approved(true);

    StepVerifier.create(responseService.respondToRequest(ACCEPTOR_ID, REQUEST_ID, body))
        .expectError(ForbiddenException.class)
        .verify();

    Mockito.verifyNoInteractions(requestRepository, workflowRepository);
  }

  @Test
  void testRespondToRequest_SaeButNotAcceptor_IsForbidden() {
    Long otherUserId = 99L;
    when(portalServiceClient.getUserById(otherUserId)).thenReturn(Mono.just(saeUser(otherUserId)));
    when(portalServiceClient.hasSaeRole(any())).thenReturn(true);
    when(requestRepository.findById(REQUEST_ID)).thenReturn(Mono.just(requestInReview()));

    ParkingResponseIn body = new ParkingResponseIn().approved(true);

    StepVerifier.create(responseService.respondToRequest(otherUserId, REQUEST_ID, body))
        .expectError(ForbiddenException.class)
        .verify();

    Mockito.verifyNoInteractions(workflowRepository);
  }

  @Test
  void testRespondToRequest_RequestNotFound() {
    when(portalServiceClient.getUserById(ACCEPTOR_ID)).thenReturn(Mono.just(saeUser(ACCEPTOR_ID)));
    when(portalServiceClient.hasSaeRole(any())).thenReturn(true);
    when(requestRepository.findById(REQUEST_ID)).thenReturn(Mono.empty());

    ParkingResponseIn body = new ParkingResponseIn().approved(true);

    StepVerifier.create(responseService.respondToRequest(ACCEPTOR_ID, REQUEST_ID, body))
        .expectError(NotFoundException.class)
        .verify();
  }

  @Test
  void testRespondToRequest_AlreadyApproved_IsConflict() {
    Request approved = requestInReview();
    approved.setIdStatus(Constants.ID_STATUS_APPROVED);

    when(portalServiceClient.getUserById(ACCEPTOR_ID)).thenReturn(Mono.just(saeUser(ACCEPTOR_ID)));
    when(portalServiceClient.hasSaeRole(any())).thenReturn(true);
    when(requestRepository.findById(REQUEST_ID)).thenReturn(Mono.just(approved));

    ParkingResponseIn body = new ParkingResponseIn().approved(true);

    StepVerifier.create(responseService.respondToRequest(ACCEPTOR_ID, REQUEST_ID, body))
        .expectError(ConflictException.class)
        .verify();

    Mockito.verifyNoInteractions(workflowRepository);
  }

  @Test
  void testRespondToRequest_AlreadyRejected_IsConflict() {
    Request rejected = requestInReview();
    rejected.setIdStatus(Constants.ID_STATUS_REJECTED);

    when(portalServiceClient.getUserById(ACCEPTOR_ID)).thenReturn(Mono.just(saeUser(ACCEPTOR_ID)));
    when(portalServiceClient.hasSaeRole(any())).thenReturn(true);
    when(requestRepository.findById(REQUEST_ID)).thenReturn(Mono.just(rejected));

    ParkingResponseIn body = new ParkingResponseIn().approved(false).comment("Comentario");

    StepVerifier.create(responseService.respondToRequest(ACCEPTOR_ID, REQUEST_ID, body))
        .expectError(ConflictException.class)
        .verify();

    Mockito.verifyNoInteractions(workflowRepository);
  }

  @Test
  void testRespondToRequest_RejectWithoutComment_IsBadRequest() {
    when(portalServiceClient.getUserById(ACCEPTOR_ID)).thenReturn(Mono.just(saeUser(ACCEPTOR_ID)));
    when(portalServiceClient.hasSaeRole(any())).thenReturn(true);
    when(requestRepository.findById(REQUEST_ID)).thenReturn(Mono.just(requestInReview()));

    ParkingResponseIn body = new ParkingResponseIn().approved(false);

    StepVerifier.create(responseService.respondToRequest(ACCEPTOR_ID, REQUEST_ID, body))
        .expectError(IllegalArgumentException.class)
        .verify();

    Mockito.verify(requestRepository, Mockito.never()).updateResponse(any(), any(), any());
    Mockito.verifyNoInteractions(workflowRepository);
  }

  @Test
  void testRespondToRequest_Approve_UpdatesRequestAndAppendsWorkflow() {
    when(portalServiceClient.getUserById(ACCEPTOR_ID)).thenReturn(Mono.just(saeUser(ACCEPTOR_ID)));
    when(portalServiceClient.hasSaeRole(any())).thenReturn(true);
    when(requestRepository.findById(REQUEST_ID)).thenReturn(Mono.just(requestInReview()), Mono.just(requestInReview()));
    when(requestRepository.updateResponse(any(), any(), any())).thenReturn(Mono.empty());
    when(workflowRepository.saveWorkflow(any(), any(), any(), any())).thenReturn(Mono.empty());

    ParkingResponseIn body = new ParkingResponseIn().approved(true).comment("Todo en orden.");

    StepVerifier.create(responseService.respondToRequest(ACCEPTOR_ID, REQUEST_ID, body))
        .expectNextCount(1)
        .verifyComplete();

    ArgumentCaptor<Integer> statusCaptor = ArgumentCaptor.forClass(Integer.class);
    Mockito.verify(requestRepository).updateResponse(eq(REQUEST_ID), statusCaptor.capture(), any(LocalDateTime.class));
    assertEquals(Constants.ID_STATUS_APPROVED, statusCaptor.getValue());

    Mockito.verify(workflowRepository).saveWorkflow(eq(REQUEST_ID), eq(Constants.ID_STATUS_APPROVED),
        any(LocalDateTime.class), eq("Todo en orden."));
    Mockito.verify(workflowRepository, Mockito.never()).saveWorkflow(any(), any(), any(),
        eq(Constants.OBSERVATION_REJECTED_DEFAULT));
  }

  @Test
  void testRespondToRequest_NeverMutatesExistingWorkflowHistory() {
    when(portalServiceClient.getUserById(ACCEPTOR_ID)).thenReturn(Mono.just(saeUser(ACCEPTOR_ID)));
    when(portalServiceClient.hasSaeRole(any())).thenReturn(true);
    when(requestRepository.findById(REQUEST_ID)).thenReturn(Mono.just(requestInReview()), Mono.just(requestInReview()));
    when(requestRepository.updateResponse(any(), any(), any())).thenReturn(Mono.empty());
    when(workflowRepository.saveWorkflow(any(), any(), any(), any())).thenReturn(Mono.empty());

    ParkingResponseIn body = new ParkingResponseIn().approved(true);

    StepVerifier.create(responseService.respondToRequest(ACCEPTOR_ID, REQUEST_ID, body))
        .expectNextCount(1)
        .verifyComplete();

    Mockito.verify(workflowRepository, Mockito.times(1)).saveWorkflow(any(), any(), any(), any());
  }
}
