package com.kpi.mars.palletizeradapter.converter;

import com.kpi.mars.palletizer.api.BuildReleaseComplete;
import com.kpi.mars.palletizeradapter.util.PersistsDeviceEventMessage;
import com.kpi.roboticshub.adapter.AdapterProperties;
import com.kpi.roboticshub.adapter.CommunicationLogService;
import com.kpi.roboticshub.adapter.mongo.ActivityRepository;
import com.kpi.roboticshub.adapter.mongo.entity.ActivityWrapper;
import com.kpi.roboticshub.api.ApiError;
import com.kpi.roboticshub.api.DeviceEvent;
import com.kpi.roboticshub.api.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.kpi.roboticshub.api.ApiErrorConstants.UNEXPECTED_FAILURE_FORMAT;
import static com.kpi.roboticshub.api.ApiErrorConstants.UNEXPECTED_FAILURE_ID;

@Component
public class BuildReleaseCompleteConverter implements Converter<BuildReleaseComplete, DeviceEvent>
{
  private static final Logger LOGGER = LogManager.getLogger();

  private final CommunicationLogService    communicationLogService;
  private final PersistsDeviceEventMessage persistsDeviceEventMessage;
  private final ActivityRepository         activityRepository;
  private final AdapterProperties          adapterProperties;

  public BuildReleaseCompleteConverter(CommunicationLogService communicationLogService,
                                       PersistsDeviceEventMessage persistsDeviceEventMessage,
                                       ActivityRepository activityRepository, AdapterProperties adapterProperties)
  {
    this.communicationLogService = communicationLogService;
    this.persistsDeviceEventMessage = persistsDeviceEventMessage;
    this.activityRepository = activityRepository;
    this.adapterProperties = adapterProperties;
  }

  @Override
  public DeviceEvent convert(BuildReleaseComplete buildReleaseComplete)
  {
    DeviceEvent deviceEvent = null;
    try
    {
      Optional<ActivityWrapper> activity = activityRepository.findById(buildReleaseComplete.getTaskId());

      deviceEvent = DeviceEvent.builder()
          .activityState(buildReleaseComplete.getStatus())
          .activityId(buildReleaseComplete.getTaskId())
          .activityType(BuildReleaseComplete.TYPE)
          .deviceId(buildReleaseComplete.getDeviceId())
          .timestamp(OffsetDateTime.now())
          .build();
      Location source = Location.builder()
          .locationId(buildReleaseComplete.getSource().getLocationId())
          .build();
      deviceEvent.setSystemId(adapterProperties.getSystemId());
      deviceEvent.setLocation(source);

      if (activity.isPresent())
      {
        deviceEvent.setDeviceType(activity.get().getDataObject().getDeviceType());
        deviceEvent.setActivityType(BuildReleaseComplete.TYPE);
      }
      communicationLogService.translated(deviceEvent, BuildReleaseComplete.class, DeviceEvent.class);
      persistsDeviceEventMessage.insertDeviceEventMessage(deviceEvent, null);
    }
    catch (Exception e)
    {
      LOGGER.error("Unable to convert message:\n{}\nException:", buildReleaseComplete, e);
      ApiError apiError = ApiError.builder().errorId(UNEXPECTED_FAILURE_ID)
          .description(UNEXPECTED_FAILURE_FORMAT.formatted(e.getMessage()))
          .build();
      persistsDeviceEventMessage.insertDeviceEventMessage(deviceEvent, List.of(apiError));
    }
    return deviceEvent;
  }
}
