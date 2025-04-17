package com.utp.reponse.controller;

import com.utp.reponse.model.Request;
import com.utp.reponse.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/response")
public class ResponseController {

    private final ResponseService responseService;

    @GetMapping
    public Flux<Request> getAllRequests() {
        return responseService.getAllRequests();
    }

}
