package com.utp.response.util;

import java.time.Duration;

public final class Constants {

  private Constants() {
    throw new IllegalStateException("Utility class");
  }

  public static final Integer ID_STATUS_IN_REVISION = 2;
  public static final Integer ID_STATUS_APPROVED = 3;
  public static final Integer ID_STATUS_REJECTED = 4;

  public static final String ROLE_NAME_SAE = "ROLE_SAE";

  public static final String NAME_MICROSERVICE = "business-parking-response";

  public static final Duration PORTAL_CLIENT_TIMEOUT = Duration.ofSeconds(5);

  public static final String OBSERVATION_APPROVED_DEFAULT = "Solicitud aprobada por Personal SAE.";
  public static final String OBSERVATION_REJECTED_DEFAULT = "Solicitud rechazada por Personal SAE.";

  public static final String ERROR_REQUEST_NOT_FOUND = "La solicitud no existe";
  public static final String ERROR_USER_NOT_SAE = "El usuario autenticado no tiene el rol ROLE_SAE";
  public static final String ERROR_NOT_ACCEPTOR =
      "El usuario autenticado no es el aceptante asignado a esta solicitud";
  public static final String ERROR_REQUEST_NOT_IN_REVIEW =
      "La solicitud no se encuentra en un estado válido para ser respondida";
  public static final String ERROR_COMMENT_REQUIRED_ON_REJECTION =
      "El comentario es obligatorio al rechazar una solicitud";
  public static final String ERROR_APPROVED_REQUIRED = "El campo approved es obligatorio";
  public static final String ERROR_USERS_SERVICE_UNAVAILABLE = "business-core-portal no se encuentra disponible";
  public static final String ERROR_USERS_SERVICE_TIMEOUT = "business-core-portal no respondió a tiempo";
}
