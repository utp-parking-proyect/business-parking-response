package com.utp.response.repository;

import com.utp.response.model.dto.VehicleDto;
import com.utp.response.model.entity.Vehicle;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface VehicleRepository extends R2dbcRepository<Vehicle, Integer> {

  @Query("""
        INSERT INTO vehicles (id_vehicle_type, id_user, number_plate, active)
        VALUES (:#{#vehicle.vehicleType},
                :#{#vehicle.idUser},
                :#{#vehicle.numberPlate},
                :#{#vehicle.active});
      """)
  Mono<Void> saveNewVehicle(@Param("vehicle") VehicleDto vehicle);
}
