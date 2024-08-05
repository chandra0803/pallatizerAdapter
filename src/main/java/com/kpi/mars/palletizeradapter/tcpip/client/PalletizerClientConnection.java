/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.tcpip.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kpi.mars.common.api.BaseMessage;
import com.kpi.mars.common.api.DeviceStatusRequest;
import com.kpi.mars.common.api.KeepAlive;
import com.kpi.mars.palletizer.api.LocationStatusRequest;
import com.kpi.mars.palletizeradapter.services.BaseMessageService;
import com.kpi.sol.tcpip.connection.AdapterClientConnection;
import com.kpi.sol.tcpip.properties.AdapterConnectionProperties;
import com.kpi.sol.tcpip.socket.AdapterClientSocket;

import java.time.LocalDateTime;

/**
 * Represents a palletizer specific {@link AdapterClientConnection}.
 * <p>
 * This is used to write all {@link BaseMessage}s to the palletizer.
 *
 * @author sja
 */
public class PalletizerClientConnection extends AdapterClientConnection<BaseMessage, AdapterClientSocket>
{
  private final BaseMessageService baseMessageService;

  private final String deviceId;

  /**
   * Creates a new instance of the {@link PalletizerClientConnection} class.
   */
  public PalletizerClientConnection(PalletizerReceiveService receiveService,
                                    String deviceId, AdapterConnectionProperties properties,
                                    BaseMessageService baseMessageService)
  {
    super(properties, receiveService::handleMessage, baseMessageService.getObjectMapper());
    this.baseMessageService = baseMessageService;
    this.deviceId = deviceId;
  }

  @Override
  protected void connected()
  {
    super.connected();
    //Send the DeviceStatusRequest
    DeviceStatusRequest deviceStatusRequest = DeviceStatusRequest.builder()
        .deviceId(deviceId)
        .sentAt(LocalDateTime.now())
        .build();
    write(deviceStatusRequest, true);
    //Send the LocationStatusRequest
    LocationStatusRequest locationStatusRequest = LocationStatusRequest.builder()
        .deviceId(deviceId).build();
    write(locationStatusRequest, true);
  }

  protected BaseMessage parseMessage(String message)
  {
    try
    {
      return baseMessageService.getBaseMessage(message);
    }
    catch (JsonProcessingException e)
    {
      logger.error("Unable to parse message:\n{}\nException:", message, e);
      return null;
    }
  }

  @Override
  protected void setMessageNumber(BaseMessage message)
  {
    message.setMessageNumber(getMessageNumber());
  }

  @Override
  protected BaseMessage createKeepAlive()
  {
    var keepAlive = new KeepAlive();
    keepAlive.setMessageNumber(getMessageNumber());
    return keepAlive;
  }
}
