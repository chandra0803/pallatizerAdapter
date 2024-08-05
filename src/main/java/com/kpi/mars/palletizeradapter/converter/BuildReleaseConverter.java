/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.converter;

import com.kpi.mars.palletizer.api.BuildRelease;
import com.kpi.mars.palletizer.api.Location;
import com.kpi.roboticshub.adapter.CommunicationLogService;
import com.kpi.roboticshub.api.Activity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.integration.config.IntegrationConverter;
import org.springframework.stereotype.Component;

/**
 * Represents a converter from {@link Activity} to {@link BuildRelease}
 *
 * @author PunyatoyaMohanty
 */
@Component
@IntegrationConverter
public class BuildReleaseConverter implements Converter<Activity, BuildRelease>
{
  private final CommunicationLogService communicationLogService;

  /**
   * Creates a new instance of the {@link BuildReleaseConverter} class.
   */
  public BuildReleaseConverter(CommunicationLogService communicationLogService)
  {
    this.communicationLogService = communicationLogService;
  }

  @Override
  public BuildRelease convert(Activity activity)
  {
    Location source = Location.builder()
        .locationId(activity.getActivityDetail().getContainers().get(0).getLocation().getLocationId())
        .build();
    source.setData(activity.getActivityDetail().getContainers().get(0).getLocation().getData());
    BuildRelease buildRelease = BuildRelease.builder()
        .buildId(activity.getActivityDetail().getActivityId())
        .deviceId(activity.getActivityDetail().getDeviceId())
        .taskId(activity.getActivityDetail().getActivityId())
        .source(source).build();
    if (activity.getData() != null)
    {
      buildRelease.getData().putAll(activity.getData());
    }
    if (activity.getActivityDetail().getData() != null)
    {
      buildRelease.getData().putAll(activity.getActivityDetail().getData());
    }
    communicationLogService.translated(buildRelease, Activity.class, BuildRelease.class);
    return buildRelease;
  }
}
