package com.utp.response.service;

import com.utp.response.generated.model.ParkingResponseIn;
import com.utp.response.model.entity.Request;
import reactor.core.publisher.Mono;

public interface ResponseService {

  Mono<Request> respondToRequest(Long authenticatedUserId, Integer requestId, ParkingResponseIn parkingResponseIn);
}
