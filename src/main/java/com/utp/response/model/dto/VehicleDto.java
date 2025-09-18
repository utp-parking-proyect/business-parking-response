package com.utp.response.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleDto {

  private Integer vehicleType;
  private Integer idUser;
  private String numberPlate;
  private Boolean active;
}
