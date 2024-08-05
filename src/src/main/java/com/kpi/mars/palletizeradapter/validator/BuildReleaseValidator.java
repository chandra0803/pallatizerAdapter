/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.validator;

import com.kpi.mars.palletizeradapter.PalletizerAdapterProperties;
import com.kpi.mars.palletizeradapter.PalletizerValidatorProperties;
import com.kpi.roboticshub.adapter.mongo.ActivityService;
import com.kpi.roboticshub.adapter.validation.ActivityValidator;
import com.kpi.roboticshub.adapter.validation.ValidationService;
import com.kpi.roboticshub.api.Activity;
import com.kpi.roboticshub.api.ApiError;
import com.kpi.sol.tcpip.properties.AdapterConnectionProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.kpi.roboticshub.api.ApiErrorConstants.*;
import static com.kpi.roboticshub.api.ApiErrorConstants.ACTIVITY_ID_DOES_NOT_EXIST_DESCRIPTION;

/**
 * Represents a {@link BuildReleaseValidator} to validate {@link Activity} object.
 *
 * @author PunyatoyaMohanty
 */

@Component
public class BuildReleaseValidator extends ActivityValidator
{
  private  final  PalletizerAdapterProperties palletizerAdapterProperties;
  private final ActivityService             activityService;

  public BuildReleaseValidator(ValidationService validationService,
                               PalletizerValidatorProperties palletizerValidatorProperties,
                               PalletizerAdapterProperties palletizerAdapterProperties,
                               ActivityService activityService)
  {
    super(validationService, palletizerValidatorProperties.getBuildReleaseValidatorProperties());
    this.palletizerAdapterProperties = palletizerAdapterProperties;
    this.activityService = activityService;
  }
  @Override
  public List<ApiError> validateExtensions(Activity activity)
  {
    String deviceId = activity.getActivityDetail().getDeviceId();
    List<ApiError> apiErrors= this.validateDeviceIdExists(deviceId);
    apiErrors.addAll(this.validateActivityIdExists(activity.getActivityDetail().getActivityId()));
    return apiErrors;
  }
 public List<ApiError> validateDeviceIdExists(String deviceId)
  {
    List<ApiError> errors = new ArrayList<>();
    Set<String> allowedDeviceIds = palletizerAdapterProperties.getClientConnections().keySet();
    AdapterConnectionProperties connectionProperties = palletizerAdapterProperties.getClientConnections().get(deviceId);
    if (connectionProperties == null)
    {
      errors.add(ApiError.builder().errorId(UNEXPECTED_VALUE_ID)
              .description(String.format(UNEXPECTED_VALUE_FORMAT, deviceId, allowedDeviceIds)).build());
    }

    return errors;
  }
  public List<ApiError> validateActivityIdExists(String activityId)
  {
    List<ApiError> errors = new ArrayList<>();
    if (activityService.getRepository().findByIdAndStatus(activityId, HttpStatus.OK) == null)
    {
      errors.add(ApiError.builder().errorId(ACTIVITY_ID_DOES_NOT_EXIST_ID)
                     .description(ACTIVITY_ID_DOES_NOT_EXIST_DESCRIPTION).build());
    }
    return errors;  }

}
