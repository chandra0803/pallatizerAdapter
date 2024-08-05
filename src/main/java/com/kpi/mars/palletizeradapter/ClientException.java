/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import com.kpi.roboticshub.api.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Represents an exception class that deals with any internal exception during the processing of request.
 *
 * @author Jacob.Richards
 */
public class ClientException extends ResponseStatusException
{
  private final transient Object message;

  private final transient List<ApiError> errors;

  public ClientException(Object message, List<ApiError> errors, HttpStatus status)
  {
    super(status, "%s client errors".formatted(errors));
    this.message = message;
    this.errors = errors;
  }

  public Object getClientMessage()
  {
    return this.message;
  }

  public List<ApiError> getErrors()
  {
    return this.errors;
  }
}
