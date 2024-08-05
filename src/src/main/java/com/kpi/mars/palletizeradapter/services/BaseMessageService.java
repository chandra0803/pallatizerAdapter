/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpi.mars.common.api.BaseMessage;
import com.kpi.mars.common.api.DeviceStatus;
import com.kpi.mars.common.api.DeviceStatusRequest;
import com.kpi.mars.palletizer.api.*;
import org.springframework.stereotype.Service;

/**
 * Represents a {@link Service} that determines the specific type for {@link BaseMessage}s.
 *
 * @author sja
 */
@Service
public class BaseMessageService
{
  private final ObjectMapper objectMapper;

  /**
   * Creates a new instance of the {@link BaseMessageService} class.
   */
  public BaseMessageService(ObjectMapper objectMapper)
  {
    this.objectMapper = objectMapper;
  }

  /**
   * @return the {@code objectMapper}
   */
  public ObjectMapper getObjectMapper()
  {
    return objectMapper;
  }

  /**
   * Gets the specific type for a {@link BaseMessage}.
   *
   * @param message the message to get a specific type for.
   * @return the message as its specific type.
   * @throws JsonProcessingException when the message cannot be parsed.
   */
  public BaseMessage getBaseMessage(String message) throws JsonProcessingException
  {
    BaseMessage baseMessage = objectMapper.readValue(message, BaseMessage.class);
    MessageType messageType = MessageType.getFromValue(baseMessage.getMessageType());
    switch (messageType)
    {
      case DEVICE_STATUS ->
      {
        return objectMapper.readValue(message, DeviceStatus.class);
      }
      case DEVICE_STATUS_REQUEST ->
      {
        return objectMapper.readValue(message, DeviceStatusRequest.class);
      }
      case BUILD_REQUEST ->
      {
        return objectMapper.readValue(message, BuildRequest.class);
      }
      case BUILD_COMPLETE ->
      {
        return objectMapper.readValue(message, BuildComplete.class);
      }
      case BUILD_RELEASE ->
      {
        return objectMapper.readValue(message, BuildRelease.class);
      }
      case BUILD_RELEASE_COMPLETE ->
      {
        return objectMapper.readValue(message, BuildReleaseComplete.class);
      }
      case LOCATION_STATUS_REQUEST ->
      {
        return objectMapper.readValue(message, LocationStatusRequest.class);
      }
      case LOCATION_STATUS ->
      {
        return objectMapper.readValue(message, LocationStatus.class);
      }
      default ->
      {
        return baseMessage;
      }
    }
  }
}
