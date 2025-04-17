package com.utp.reponse.repository;

import com.utp.reponse.model.Request;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends R2dbcRepository<Request, Integer> {
}
