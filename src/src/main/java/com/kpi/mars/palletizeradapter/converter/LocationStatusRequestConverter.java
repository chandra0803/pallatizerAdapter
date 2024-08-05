/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2023 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.converter;

import com.kpi.mars.palletizer.api.Location;
import com.kpi.mars.palletizer.api.LocationStatusRequest;
import com.kpi.roboticshub.adapter.CommunicationLogService;
import com.kpi.roboticshub.api.DeviceRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class LocationStatusRequestConverter implements Converter<DeviceRequest, LocationStatusRequest>
{
  private final CommunicationLogService communicationLogService;

  public LocationStatusRequestConverter(CommunicationLogService communicationLogService)
  {
    this.communicationLogService = communicationLogService;
  }

  @Override
  public LocationStatusRequest convert(@Nullable DeviceRequest deviceRequest)
  {
    if (deviceRequest == null)
    {
      return null;
    }

    LocationStatusRequest locationStatusRequest = LocationStatusRequest.builder()
        .deviceId(deviceRequest.getDeviceId())
        .build();

    if (deviceRequest.getLocationId() != null)
    {
      locationStatusRequest.setLocation(Location.builder().locationId(deviceRequest.getLocationId()).build());
    }

    communicationLogService.translated(deviceRequest, DeviceRequest.class, LocationStatusRequest.class);
    return locationStatusRequest;
  }
}