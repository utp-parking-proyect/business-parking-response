package com.utp.response.repository;

import com.utp.response.model.dto.RequestDto;
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
      UPDATE requests
      SET date_response = :dateResponse,
          approved = :#{#requestDto.approved},
          comment = :#{#requestDto.comment},
          id_status = :#{#requestDto.statusId}
      WHERE id_request = :idRequest;
      """)
  Mono<Void> updateRequest(
      @Param("idRequest") Integer idRequest,
      @Param("requestDto") RequestDto requestDto,
      @Param("dateResponse") LocalDateTime dateResponse
  );

}
