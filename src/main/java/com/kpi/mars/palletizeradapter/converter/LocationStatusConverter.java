/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2023 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.converter;

import com.kpi.mars.palletizeradapter.PalletizerAdapterProperties;
import com.kpi.mars.palletizer.api.LocationStatus;
import com.kpi.roboticshub.adapter.AdapterProperties;
import com.kpi.roboticshub.adapter.CommunicationLogService;
import com.kpi.roboticshub.api.DeviceInfo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LocationStatusConverter implements Converter<LocationStatus, DeviceInfo>
{

  private final CommunicationLogService     communicationLogService;
  private final PalletizerAdapterProperties palletizerAdapterProperties;
  private final AdapterProperties           adapterProperties;

  public LocationStatusConverter(CommunicationLogService communicationLogService,
                                 PalletizerAdapterProperties palletizerAdapterProperties,
                                 AdapterProperties adapterProperties)
  {
    this.communicationLogService = communicationLogService;
    this.palletizerAdapterProperties = palletizerAdapterProperties;
    this.adapterProperties = adapterProperties;
  }

  public DeviceInfo convert(LocationStatus locationStatus)
  {
    if (locationStatus == null)
    {
      return null;
    }

    DeviceInfo deviceInfo = DeviceInfo.builder()
        .systemId(adapterProperties.getSystemId())
        .deviceId(locationStatus.getDeviceId())
        .deviceType(palletizerAdapterProperties.getDeviceType())
        .locationId(locationStatus.getLocation() != null ? locationStatus.getLocation().getLocationId() : null)
        .containerId(locationStatus.getContainer() != null ? locationStatus.getContainer().getContainerId() : null)
        .status(locationStatus.getStatus() != null ? locationStatus.getStatus() : "UNDEFINED")
        .build();
    communicationLogService.translated(deviceInfo, LocationStatus.class, DeviceInfo.class);
    return deviceInfo;
  }
}
