package com.utp.response.repository;

import com.utp.response.model.entity.Request;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface RequestRepository extends R2dbcRepository<Request, Integer> {

  @Query("""
      UPDATE parking_requests
      SET id_status = :idStatus,
          date_response = :dateResponse
      WHERE id_request = :idRequest;
      """)
  Mono<Void> updateResponse(@Param("idRequest") Integer idRequest,
                            @Param("idStatus") Integer idStatus,
                            @Param("dateResponse") LocalDateTime dateResponse);

}
