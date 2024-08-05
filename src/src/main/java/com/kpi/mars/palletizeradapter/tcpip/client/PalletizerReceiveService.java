/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.tcpip.client;

import com.kpi.mars.common.api.BaseMessage;
import com.kpi.mars.common.api.DeviceStatus;
import com.kpi.mars.palletizer.api.BuildComplete;
import com.kpi.mars.palletizer.api.BuildReleaseComplete;
import com.kpi.mars.palletizer.api.LocationStatus;
import com.kpi.mars.palletizer.api.MessageType;
import com.kpi.mars.palletizeradapter.util.PersistsDeviceEventMessage;
import com.kpi.roboticshub.adapter.CommunicationLogService;
import com.kpi.roboticshub.adapter.validation.ValidationException;
import com.kpi.roboticshub.api.DeviceEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * Represents a palletizer receive service.
 *
 * @author sja
 */
@Service
public class PalletizerReceiveService
{
  private static final Logger LOGGER = LogManager.getLogger();

  private final MessageChannel adapterSendChannel;

  private final CommunicationLogService    communicationLogService;
  private final PersistsDeviceEventMessage persistsDeviceEventMessage;

  /**
   * Creates a new instance of the {@link PalletizerReceiveService} class.
   */
  public PalletizerReceiveService(MessageChannel adapterSendChannel,
                                  CommunicationLogService communicationLogService,
                                  PersistsDeviceEventMessage persistsDeviceEventMessage)
  {
    this.adapterSendChannel = adapterSendChannel;
    this.communicationLogService = communicationLogService;
    this.persistsDeviceEventMessage = persistsDeviceEventMessage;
  }

  public BaseMessage handleMessage(BaseMessage message)
  {
    MessageType messageType = MessageType.getFromValue(message.getMessageType());
    switch (messageType)
    {
      case KEEP_ALIVE ->
      {
        if (message.getRequestNumber() == null)
        {
          // requires response
          message.setRequestNumber(message.getMessageNumber());
          return message;
        }
        else
        {
          // requires no response
          return null;
        }
      }
      case DEVICE_STATUS ->
      {
        //Log the status message
        DeviceStatus deviceStatus = (DeviceStatus) message;
        communicationLogService.received(message, deviceStatus.getDeviceId(), null);
        return null;
      }
      case LOCATION_STATUS ->
      {
        LocationStatus locationStatus = (LocationStatus) message;
        communicationLogService.received(message, locationStatus.getDeviceId(), null);

        adapterSendChannel.send(MessageBuilder.withPayload(message).build());

        return null;
      }
      case BUILD_COMPLETE ->
      {
        BuildComplete buildComplete = (BuildComplete) message;
        communicationLogService.received(message, buildComplete.getDeviceId(), null);
        try
        {
          adapterSendChannel.send(MessageBuilder.withPayload(message).build());
        }
        catch (MessageHandlingException me)
        {
          if (me.getCause() instanceof ValidationException ve)
          {
            if (ve.getClientMessage() instanceof DeviceEvent)
            {
              persistsDeviceEventMessage.insertDeviceEventMessage(
                  ((DeviceEvent) ve.getClientMessage()), ve.getErrors());
            }
            else
            {
              throw ve;
            }
          }
        }
        return null;
      }
      case BUILD_RELEASE_COMPLETE ->
      {
        BuildReleaseComplete buildReleaseComplete = (BuildReleaseComplete) message;
        communicationLogService.received(message, buildReleaseComplete.getDeviceId(), null);
        try
        {
          adapterSendChannel.send(MessageBuilder.withPayload(message).build());
        }
        catch (MessageHandlingException me)
        {
          if (me.getCause() instanceof ValidationException ve)
          {
            if (ve.getClientMessage() instanceof DeviceEvent)
            {
              persistsDeviceEventMessage.insertDeviceEventMessage(
                  ((DeviceEvent) ve.getClientMessage()), ve.getErrors());
            }
            else
            {
              throw ve;
            }
          }
        }
        return null;
      }
      case DEVICE_STATUS_REQUEST, BUILD_REQUEST, BUILD_RELEASE, LOCATION_STATUS_REQUEST ->
      {
        LOGGER.info("Message type [{}] is not expected in this direction.",
                    message.getMessageType());
        return null;
      }
      default ->
      {
        LOGGER.error("Message type [{}] is not expected.", message.getMessageType());
        return null;
      }
    }
  }
}
