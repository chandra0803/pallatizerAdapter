/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.services;

import com.kpi.mars.palletizeradapter.PalletizerActivityService;
import com.kpi.roboticshub.adapter.AdapterProperties;
import com.kpi.roboticshub.adapter.service.DefaultRoboticHubReceiveService;
import com.kpi.roboticshub.adapter.service.RoboticsHubReceiveService;
import com.kpi.roboticshub.api.*;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.handler.ReplyRequiredException;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents a palletizer {@link RoboticsHubReceiveService}.
 *
 * @author sja
 */
@Service
public class PalletizerRoboticsHubReceiveService extends DefaultRoboticHubReceiveService
{
  private final PalletizerActivityService palletizerActivityService;
  private final MessageChannel            adapterReceiveChannel;

  /**
   * Creates a new instance of the {@link PalletizerRoboticsHubReceiveService} class.
   */
  public PalletizerRoboticsHubReceiveService(PalletizerActivityService palletizerActivityService,
                                             MessageChannel adapterReceiveChannel, AdapterProperties adapterProperties)
  {
    super(adapterProperties);
    this.palletizerActivityService = palletizerActivityService;
    this.adapterReceiveChannel = adapterReceiveChannel;
  }

  @Override
  public Response<Activity> getActivityById(String activityId)
  {
    return null;
  }

  @Override
  public Response<Activity> processActivity(Activity activity)
  {
    return palletizerActivityService.processActivity(activity);
  }

  @Override
  public Response<Activity> updateActivity(Activity activity)
  {
    return null;
  }

  @Override
  public boolean deleteActivity(String activityId)
  {
    return false;
  }

  @Override
  public ResponseEntity<List<DeviceEvent>> getDeviceEventById(String identifier)
  {
    return null;
  }

  @Override
  public ResponseEntity<List<DeviceEvent>> findAllByTimeStamp(OffsetDateTime timeStamp)
  {
    return null;
  }

  @Override
  public ResponseEntity<Property> getDeviceProperty(String propertyId)
  {
    return null;
  }

  @Override
  public ResponseEntity<Property> addDeviceProperty(Property property)
  {
    return null;
  }

  public Response<DeviceRequest> processDeviceRequest(DeviceRequest deviceRequest)
  {
    try
    {
      adapterReceiveChannel.send(MessageBuilder.withPayload(deviceRequest).build());

    }
    catch (ReplyRequiredException e)
    {
      return null;
    }
    return Response.<DeviceRequest>builder().message(deviceRequest).build();
  }

}
