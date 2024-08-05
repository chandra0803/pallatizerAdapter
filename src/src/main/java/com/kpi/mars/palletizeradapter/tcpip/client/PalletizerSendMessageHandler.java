/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.tcpip.client;

import com.kpi.mars.common.api.BaseMessage;
import com.kpi.mars.common.api.DeviceStatusRequest;
import com.kpi.mars.palletizer.api.BuildRelease;
import com.kpi.mars.palletizer.api.BuildRequest;
import com.kpi.mars.palletizer.api.LocationStatusRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Represents a {@link AbstractMessageHandler} that sends {@link BaseMessage}s to palletizer endpoints.
 * <p>
 *
 * @author sja
 */
@Component
public class PalletizerSendMessageHandler extends AbstractMessageHandler
{
  private static final Logger LOGGER = LogManager.getLogger();

  private final Map<String, PalletizerClientConnection> clientConnections;
  private final Executor                                executor;

  /**
   * Creates a new instance of the {@link PalletizerSendMessageHandler} class.
   */
  public PalletizerSendMessageHandler(Map<String, PalletizerClientConnection> clientConnections)
  {
    this.clientConnections = clientConnections;
    this.setLoggingEnabled(false);
    this.executor = Executors.newCachedThreadPool();
  }

  protected void processMessageInternal(Message<?> message)
  {

    if (message.getPayload() instanceof BaseMessage baseMessage)
    {
      if (baseMessage instanceof BuildRequest buildRequest)
      {
        writeMessage(buildRequest.getDeviceId(), baseMessage);
      }
      else if (baseMessage instanceof DeviceStatusRequest deviceStatusRequest)
      {
        writeMessage(deviceStatusRequest.getDeviceId(), baseMessage);
      }
      else if (baseMessage instanceof BuildRelease buildRelease)
      {
        writeMessage(buildRelease.getDeviceId(), baseMessage);
      }
      else if (baseMessage instanceof LocationStatusRequest locationStatusRequest)
      {
        writeMessage(locationStatusRequest.getDeviceId(), baseMessage);
      }
      else
      {
        LOGGER.error("Unexpected message type [{}].", baseMessage.getClass());
      }
    }
    else
    {
      LOGGER.error("Unexpected message payload type [{}].",
                   message.getPayload().getClass().getSimpleName());
    }
  }

  private void writeMessage(String deviceId, BaseMessage message)
  {
    PalletizerClientConnection clientConnection = clientConnections.get(deviceId);
    if (clientConnection != null)
    {
      clientConnection.write(message, true);
    }
    else
    {
      LOGGER.error("No connection is configured for device [{}].", deviceId);
    }
  }

  protected void handleMessageInternal(Message<?> message)
  {
    CompletableFuture.runAsync(() -> {
      try
      {
        processMessageInternal(message);
      }
      catch (Exception e)
      {
        LOGGER.warn("Interrupted! due to ", e);
        Thread.currentThread().interrupt();
      }
    }, executor);
  }
}
