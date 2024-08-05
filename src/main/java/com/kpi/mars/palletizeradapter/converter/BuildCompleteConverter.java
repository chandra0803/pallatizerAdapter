package com.kpi.mars.palletizeradapter.converter;

import com.kpi.mars.palletizer.api.BuildComplete;
import com.kpi.mars.palletizeradapter.PalletizerAdapterProperties;
import com.kpi.mars.palletizeradapter.util.PersistsDeviceEventMessage;
import com.kpi.roboticshub.adapter.AdapterProperties;
import com.kpi.roboticshub.adapter.CommunicationLogService;
import com.kpi.roboticshub.adapter.mongo.ActivityRepository;
import com.kpi.roboticshub.adapter.mongo.entity.ActivityWrapper;
import com.kpi.roboticshub.api.ApiError;
import com.kpi.roboticshub.api.Container;
import com.kpi.roboticshub.api.DeviceEvent;
import com.kpi.roboticshub.api.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.kpi.roboticshub.api.ApiErrorConstants.UNEXPECTED_FAILURE_FORMAT;
import static com.kpi.roboticshub.api.ApiErrorConstants.UNEXPECTED_FAILURE_ID;

@Component
public class BuildCompleteConverter implements Converter<BuildComplete, DeviceEvent>
{
  private static final Logger LOGGER = LogManager.getLogger();

  private final CommunicationLogService     communicationLogService;
  private final PalletizerAdapterProperties palletizerAdapterProperties;
  private final PersistsDeviceEventMessage  persistsDeviceEventMessage;
  private final ActivityRepository          activityRepository;
  private final AdapterProperties           adapterProperties;

  public BuildCompleteConverter(CommunicationLogService communicationLogService,
                                PalletizerAdapterProperties palletizerAdapterProperties,
                                PersistsDeviceEventMessage persistsDeviceEventMessage,
                                ActivityRepository activityRepository, AdapterProperties adapterProperties)
  {
    this.communicationLogService = communicationLogService;
    this.palletizerAdapterProperties = palletizerAdapterProperties;
    this.persistsDeviceEventMessage = persistsDeviceEventMessage;
    this.activityRepository = activityRepository;
    this.adapterProperties = adapterProperties;
  }

  @Override
  public DeviceEvent convert(@Nullable BuildComplete buildComplete)
  {
    if (buildComplete == null)
    {
      return null;
    }
    DeviceEvent deviceEvent = null;
    try
    {
      Optional<ActivityWrapper> activity;

      if (buildComplete.getTaskId() != null)
      {
        activity = activityRepository.findById(buildComplete.getTaskId());
      }
      else
      {
        activity = Optional.empty();
      }

      deviceEvent = DeviceEvent.builder()
          .activityState(buildComplete.getStatus())
          .activityId(buildComplete.getTaskId())
          .activityType(BuildComplete.TYPE)
          .deviceId(buildComplete.getDeviceId())
          .timestamp(OffsetDateTime.now())
          .build();
      Location destination = Location.builder()
          .locationId(buildComplete.getDestination() != null
                      ? buildComplete.getDestination().getLocationId()
                      : null)
          .build();
      Container container = Container.builder()
          .containerId(buildComplete.getContainer() != null
                       ? buildComplete.getContainer().getContainerId()
                       : null)
          .location(destination)
          .build();
      deviceEvent.setSystemId(adapterProperties.getSystemId());
      deviceEvent.setContainer(List.of(container));
      deviceEvent.setLocation(destination);
      deviceEvent.setData(buildComplete.getData());
      if (!palletizerAdapterProperties.getBuildCompleteSuccessStatuses().contains(buildComplete.getStatus()))
      {
        // error status
        deviceEvent.setErrorCode(buildComplete.getStatus());
      }

      if (activity.isPresent())
      {
        deviceEvent.setDeviceType(activity.get().getDataObject().getDeviceType());
        deviceEvent.setActivityType(BuildComplete.TYPE);
      }
      communicationLogService.translated(deviceEvent, BuildComplete.class, DeviceEvent.class);
      persistsDeviceEventMessage.insertDeviceEventMessage(deviceEvent, null);
    }
    catch (Exception e)
    {
      LOGGER.error("Unable to convert message:\n{}\nException:", buildComplete, e);
      ApiError apiError = ApiError.builder().errorId(UNEXPECTED_FAILURE_ID)
          .description(UNEXPECTED_FAILURE_FORMAT.formatted(e.getMessage()))
          .build();
      persistsDeviceEventMessage.insertDeviceEventMessage(deviceEvent, List.of(apiError));
    }
    return deviceEvent;
  }
}
