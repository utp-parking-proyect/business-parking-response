package com.utp.response.controller;

import com.utp.response.model.dto.RequestDto;
import com.utp.response.model.entity.Request;
import com.utp.response.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/response")
public class ResponseController {

  private final ResponseService responseService;

  @GetMapping
  public Flux<Request> getAllRequests() {
    return responseService.getAllRequests();
  }

  @PatchMapping("/{requestId}")
  public Mono<Request> responseRequest(@PathVariable Integer requestId,
                                       @RequestBody RequestDto request) {
    return responseService.responseRequest(requestId, request);
  }

}
