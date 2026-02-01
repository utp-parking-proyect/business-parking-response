package com.utp.response.service.impl;

import com.utp.response.model.dto.RequestDto;
import com.utp.response.model.dto.VehicleDto;
import com.utp.response.model.entity.Request;
import com.utp.response.repository.RequestRepository;
import com.utp.response.repository.VehicleRepository;
import com.utp.response.repository.WorkflowRepository;
import com.utp.response.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static com.utp.response.util.Constants.ID_STATUS_APPROVED;
import static com.utp.response.util.Constants.ID_STATUS_IN_REVISION;

@RequiredArgsConstructor
@Service
public class ResponseServiceImpl implements ResponseService {

  private final RequestRepository requestRepository;
  private final VehicleRepository vehicleRepository;
  private final WorkflowRepository workflowRepository;

  @Override
  public Flux<Request> getAllRequests() {
    return requestRepository.findAll();
  }

  @Override
  public Mono<Request> responseRequest(Integer requestId, RequestDto request) {
    return validateRequest(requestId)
        .then(updateRequest(requestId, request))
        .then(processVehicleIfNeeded(requestId, request))
        .flatMap(this::updateWorkflow)
        .then(saveWorkflow(requestId, request.getStatusId()))
        .then(requestRepository.findById(requestId));
  }

  private Mono<Void> updateRequest(Integer requestId, RequestDto request) {
    return requestRepository.updateRequest(requestId, request, LocalDateTime.now()).then();
  }

  private Mono<Integer> processVehicleIfNeeded(Integer requestId, RequestDto request) {
    if (request.getStatusId().equals(ID_STATUS_APPROVED)) {
      return requestRepository.findById(requestId)
          .flatMap(req -> vehicleRepository.saveNewVehicle(VehicleDto.builder()
              .vehicleType(req.getIdVehicleType())
              .idUser(req.getIdApplicant())
              .numberPlate(req.getNumberPlate())
              .active(true)
              .build()))
          .thenReturn(requestId);
    }
    return Mono.just(requestId);
  }

  private Mono<Void> updateWorkflow(Integer requestId) {
    return workflowRepository.selectWorkflowBefore(requestId, ID_STATUS_IN_REVISION)
        .flatMap(idWorkflow -> workflowRepository
            .updateDateUpdateInWorkflow(idWorkflow, LocalDateTime.now()))
        .then();
  }

  private Mono<Void> saveWorkflow(Integer requestId, Integer statusId) {
    return workflowRepository.saveWorkflow(requestId, statusId, LocalDateTime.now()).then();
  }

  private Mono<String> validateRequest(Integer requestId) {
    return requestRepository.findById(requestId)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Request not found: " + requestId)))
        .flatMap(request -> {
          if (ID_STATUS_IN_REVISION.equals(request.getIdStatus())) {
            return Mono.just("Valid");
          } else {
            return Mono.error(new IllegalStateException(
                "The request is not in a valid state for response."));
          }
        });
  }
}
