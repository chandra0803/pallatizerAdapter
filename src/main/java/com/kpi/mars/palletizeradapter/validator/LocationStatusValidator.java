/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2023 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.validator;

import com.kpi.mars.palletizeradapter.PalletizerValidatorProperties;
import com.kpi.roboticshub.adapter.validation.DeviceInfoValidator;
import com.kpi.roboticshub.adapter.validation.ValidationService;
import org.springframework.stereotype.Component;

@Component
public class LocationStatusValidator extends DeviceInfoValidator
{

  public LocationStatusValidator(ValidationService validationService,
                                 PalletizerValidatorProperties palletizerValidatorProperties)
  {
    super(validationService, palletizerValidatorProperties.getLocationStatusValidatorProperties());
  }
}
