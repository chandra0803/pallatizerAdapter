package com.kpi.mars.palletizeradapter.util;

import com.kpi.roboticshub.adapter.mongo.DeviceEventService;
import com.kpi.roboticshub.adapter.mongo.entity.DeviceEventWrapper;
import com.kpi.roboticshub.api.ApiError;
import com.kpi.roboticshub.api.DeviceEvent;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class PersistsDeviceEventMessage
{
  private DeviceEventService deviceEventService;

  private PersistsDeviceEventMessage(DeviceEventService deviceEventService)
  {
    this.deviceEventService = deviceEventService;
  }

  public DeviceEventWrapper insertDeviceEventMessage(DeviceEvent deviceEvent, List<ApiError> apiErrors)
  {
    return deviceEventService.save(UUID.randomUUID().toString(), OffsetDateTime.now(),
                                   deviceEvent,
                                   CollectionUtils.isEmpty(apiErrors) ? HttpStatus.OK
                                                                      : HttpStatus.BAD_REQUEST,
                                   apiErrors);
  }
}
