/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.validator;

import com.kpi.mars.palletizeradapter.PalletizerAdapterProperties;
import com.kpi.mars.palletizeradapter.PalletizerValidatorProperties;
import com.kpi.roboticshub.adapter.validation.ActivityValidator;
import com.kpi.roboticshub.adapter.validation.ValidationService;
import com.kpi.roboticshub.api.Activity;
import com.kpi.roboticshub.api.ApiError;
import com.kpi.sol.tcpip.properties.AdapterConnectionProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.kpi.roboticshub.api.ApiErrorConstants.*;

/**
 * Represents a {@link BuildRequestValidator} to validate {@link Activity} object.
 *
 * @author PravinVerma
 */

@Component
public class BuildRequestValidator extends ActivityValidator
{
  private  final PalletizerAdapterProperties palletizerAdapterProperties;
  public BuildRequestValidator(ValidationService validationService,
                               PalletizerValidatorProperties palletizerValidatorProperties,
                               PalletizerAdapterProperties palletizerAdapterProperties)
  {
    super(validationService, palletizerValidatorProperties.getBuildRequestValidatorProperties());
    this.palletizerAdapterProperties = palletizerAdapterProperties;
  }
  @Override
  public List<ApiError> validateExtensions(Activity activity)
  {
    String deviceId = activity.getActivityDetail().getDeviceId();
    return this.validateDeviceIdExists(deviceId);
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

}
