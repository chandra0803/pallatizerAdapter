/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import com.kpi.mars.palletizer.api.BuildComplete;
import com.kpi.sol.tcpip.properties.AdapterConnectionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;
import java.util.Map;

/**
 * Represents {@link PalletizerAdapterApplication} properties.
 *
 * @author sja
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "palletizer-adapter")
public class PalletizerAdapterProperties
{
  private final String                                   deviceType;
  private final Map<String, AdapterConnectionProperties> clientConnections;
  private final List<String>                             buildCompleteSuccessStatuses;

  /**
   * Creates a new instance of the {@link PalletizerAdapterProperties} class.
   *
   * @param clientConnections the client connection configurations keyed by device ID.
   * @param buildCompleteSuccessStatuses the list of all {@link BuildComplete} success statuses.
   */
  public PalletizerAdapterProperties(String deviceType,
                                     Map<String, AdapterConnectionProperties> clientConnections,
                                     @DefaultValue("Success") List<String> buildCompleteSuccessStatuses)
  {
    this.deviceType = deviceType;
    this.clientConnections = clientConnections;
    this.buildCompleteSuccessStatuses = buildCompleteSuccessStatuses;
  }

  /**
   * @return the {@code deviceType}
   */
  public String getDeviceType()
  {
    return deviceType;
  }

  /**
   * @return the {@code clientConnections}
   */
  public Map<String, AdapterConnectionProperties> getClientConnections()
  {
    return clientConnections;
  }

  /**
   * @return the {@code buildCompleteSuccessStatuses}
   */
  public List<String> getBuildCompleteSuccessStatuses()
  {
    return buildCompleteSuccessStatuses;
  }
}
