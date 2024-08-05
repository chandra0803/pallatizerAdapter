/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.converter;

import com.kpi.mars.palletizer.api.BuildRequest;
import com.kpi.mars.palletizer.api.Container;
import com.kpi.mars.palletizer.api.Location;
import com.kpi.roboticshub.adapter.CommunicationLogService;
import com.kpi.roboticshub.adapter.service.LocationService;
import com.kpi.roboticshub.api.Activity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.integration.config.IntegrationConverter;
import org.springframework.stereotype.Component;

/**
 * Represents a converter from {@link Activity} to {@link BuildRequest}
 *
 * @author PravinVerma
 */
@Component
@IntegrationConverter
public class BuildRequestConverter implements Converter<Activity, BuildRequest>
{
  private static final Logger                  LOGGER = LogManager.getLogger();
  private final        CommunicationLogService communicationLogService;
  private final        LocationService         locationService;

  /**
   * Creates a new instance of the {@link BuildRequestConverter} class.
   */
  public BuildRequestConverter(CommunicationLogService communicationLogService, LocationService locationService)
  {
    this.communicationLogService = communicationLogService;
    this.locationService = locationService;
  }

  @Override
  public BuildRequest convert(Activity activity)
  {
    Container container = Container.builder()
        .containerId(activity.getActivityDetail().getContainers().get(0).getContainerId())
        .build();
    container.setData(activity.getActivityDetail().getContainers().get(0).getData());

    String sourceLocation = activity.getActivityDetail().getContainers().get(0).getLocation().getLocationId();
    LOGGER.trace("source location before translation: {}", sourceLocation);

    if (locationService.getSubsystemLocationId(
        sourceLocation) != null)
    {
      sourceLocation = locationService.getSubsystemLocationId(sourceLocation);
      LOGGER.trace("source location after translation: {}", sourceLocation);
    }

    Location source = Location.builder()
        .locationId(sourceLocation)
        .data(activity.getActivityDetail().getContainers().get(0).getLocation().getData())
        .build();

    Location destination = Location.builder()
        .locationId(activity.getActivityDetail().getDestination().getLocationId())
        .data(activity.getActivityDetail().getDestination().getData())
        .build();

    BuildRequest buildRequest = BuildRequest.builder()
        .buildId(activity.getActivityDetail().getActivityId())
        .deviceId(activity.getActivityDetail().getDeviceId())
        .container(container)
        .destination(destination)
        .taskId(activity.getActivityDetail().getActivityId())
        .placement(activity.getActivityDetail().getMode())
        .source(source)
        .build();

    if (activity.getData() != null)
    {
      buildRequest.getData().putAll(activity.getData());
    }
    if (activity.getActivityDetail().getData() != null)
    {
      buildRequest.getData().putAll(activity.getActivityDetail().getData());
    }
    communicationLogService.translated(buildRequest, Activity.class, BuildRequest.class);
    return buildRequest;
  }

}
