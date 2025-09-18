package com.utp.response.repository;

import com.utp.response.model.entity.Workflow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface WorkflowRepository extends R2dbcRepository<Workflow, Integer> {
  @Query(value = """
      INSERT INTO workflow (id_request, id_status, date_create)
      VALUES (:requestId, :statusId, :dateCreate);
      """)
  Mono<Void> saveWorkflow(@Param("requestId") int requestId,
                          @Param("statusId") int statusId,
                          @Param("dateCreate") LocalDateTime dateCreate);

  @Query(value = """
      UPDATE workflow
      SET date_update = :dateUpdate
      WHERE id_workflow = :workflowId
      """)
  Mono<Void> updateDateUpdateInWorkflow(@Param("workflowId") int workflowId,
                                        @Param("dateUpdate") LocalDateTime dateUpdate);

  @Query(value = """
      SELECT w.id_workflow
      FROM workflow w
      WHERE w.id_request = :idRequest
      AND id_status = :statusId;
      """)
  Mono<Integer> selectWorkflowBefore(@Param("idRequest") Integer idRequest,
                                     @Param("statusId") Integer statusId);
}
