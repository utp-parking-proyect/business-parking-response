package com.utp.reponse.service;

import com.utp.reponse.model.Request;
import reactor.core.publisher.Flux;

public interface ResponseService {
    Flux<Request> getAllRequests();
}
