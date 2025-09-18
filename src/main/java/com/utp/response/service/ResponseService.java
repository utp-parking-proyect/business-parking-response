package com.utp.response.service;

import com.utp.response.model.dto.RequestDto;
import com.utp.response.model.entity.Request;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ResponseService {

  Flux<Request> getAllRequests();
  Mono<Request> responseRequest(Integer requestId, RequestDto request);
}
