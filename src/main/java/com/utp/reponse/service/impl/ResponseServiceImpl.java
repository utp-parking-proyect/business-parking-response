package com.utp.reponse.service.impl;

import com.utp.reponse.model.Request;
import com.utp.reponse.repository.RequestRepository;
import com.utp.reponse.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class ResponseServiceImpl implements ResponseService {

    private final RequestRepository requestRepository;

    @Override
    public Flux<Request> getAllRequests() {
        return requestRepository.findAll();
    }
}
