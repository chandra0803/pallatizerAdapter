/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import com.kpi.roboticshub.adapter.validation.properties.ActivityValidatorProperties;
import com.kpi.roboticshub.adapter.validation.properties.DeviceEventValidatorProperties;
import com.kpi.roboticshub.adapter.validation.properties.DeviceInfoValidatorProperties;
import com.kpi.roboticshub.adapter.validation.properties.DeviceRequestValidatorProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Represents validator properties.
 *
 * @author PravinVerma
 */
@ConstructorBinding
@ConfigurationProperties("palletizer-validator")
public class PalletizerValidatorProperties
{
  @NestedConfigurationProperty
  private final ActivityValidatorProperties    buildRequestValidatorProperties;
  @NestedConfigurationProperty
  private final DeviceEventValidatorProperties buildCompleteValidatorProperties;

  @NestedConfigurationProperty
  private final ActivityValidatorProperties    buildReleaseValidatorProperties;
  @NestedConfigurationProperty
  private final DeviceEventValidatorProperties buildReleaseCompleteValidatorProperties;

  @NestedConfigurationProperty
  private final DeviceRequestValidatorProperties locationStatusRequestValidatorProperties;

  @NestedConfigurationProperty
  private final DeviceInfoValidatorProperties locationStatusValidatorProperties;

  public PalletizerValidatorProperties(@DefaultValue ActivityValidatorProperties buildRequestValidatorProperties,
                                       @DefaultValue DeviceEventValidatorProperties buildCompleteValidatorProperties,
                                       @DefaultValue ActivityValidatorProperties buildReleaseValidatorProperties,
                                       @DefaultValue
                                       DeviceEventValidatorProperties buildReleaseCompleteValidatorProperties,
                                       @DefaultValue
                                       DeviceRequestValidatorProperties locationStatusRequestValidatorProperties,
                                       @DefaultValue
                                       DeviceInfoValidatorProperties locationStatusValidatorProperties)
  {
    this.buildRequestValidatorProperties = buildRequestValidatorProperties;
    this.buildCompleteValidatorProperties = buildCompleteValidatorProperties;
    this.buildReleaseValidatorProperties = buildReleaseValidatorProperties;
    this.buildReleaseCompleteValidatorProperties = buildReleaseCompleteValidatorProperties;
    this.locationStatusRequestValidatorProperties = locationStatusRequestValidatorProperties;
    this.locationStatusValidatorProperties = locationStatusValidatorProperties;
  }

  public ActivityValidatorProperties getBuildRequestValidatorProperties()
  {
    return buildRequestValidatorProperties;
  }

  public DeviceEventValidatorProperties getBuildCompleteValidatorProperties()
  {
    return buildCompleteValidatorProperties;
  }

  public ActivityValidatorProperties getBuildReleaseValidatorProperties()
  {
    return buildReleaseValidatorProperties;
  }

  public DeviceEventValidatorProperties getBuildReleaseCompleteValidatorProperties()
  {
    return buildReleaseCompleteValidatorProperties;
  }

  public DeviceRequestValidatorProperties getLocationStatusRequestValidatorProperties()
  {
    return locationStatusRequestValidatorProperties;
  }

  public DeviceInfoValidatorProperties getLocationStatusValidatorProperties()
  {
    return locationStatusValidatorProperties;
  }
}
