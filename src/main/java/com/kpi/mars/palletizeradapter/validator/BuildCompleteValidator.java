package com.kpi.mars.palletizeradapter.validator;

import com.kpi.mars.palletizeradapter.PalletizerValidatorProperties;
import com.kpi.roboticshub.adapter.validation.DeviceEventValidator;
import com.kpi.roboticshub.adapter.validation.ValidationService;
import org.springframework.stereotype.Component;

@Component
public class BuildCompleteValidator extends DeviceEventValidator
{
  public BuildCompleteValidator(ValidationService validationService,
                                PalletizerValidatorProperties palletizerValidatorProperties)

  {
    super(validationService, palletizerValidatorProperties.getBuildCompleteValidatorProperties());
  }

}
