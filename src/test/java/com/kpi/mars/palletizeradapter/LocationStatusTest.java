/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2023 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpi.mars.palletizer.api.LocationStatus;
import com.kpi.mars.palletizeradapter.converter.LocationStatusConverter;
import com.kpi.mars.palletizeradapter.validator.LocationStatusValidator;
import com.kpi.roboticshub.api.DeviceInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocationStatusTest
{
  @Autowired
  private ObjectMapper            objectMapper;
  @Autowired
  private LocationStatusConverter converter;
  @Autowired
  private LocationStatusValidator validator;

  @Test
  void convertAndValidateOne() throws IOException
  {
    LocationStatus locationStatus = loadDeviceInfo("/sample-messages/from-palletizer/location-status-1.json");
    // convert
    DeviceInfo deviceInfo = converter.convert(locationStatus);
    assertNotNull(deviceInfo);
    assertEquals(locationStatus.getLocation().getLocationId(), deviceInfo.getLocationId());
    assertEquals(locationStatus.getContainer().getContainerId(), deviceInfo.getContainerId());

    // validate
    validator.validate(deviceInfo);
  }

  @Test
  void convertAndValidateTwo() throws IOException
  {
    LocationStatus locationStatus = loadDeviceInfo("/sample-messages/from-palletizer/location-status-2.json");
    // convert
    DeviceInfo deviceInfo = converter.convert(locationStatus);
    assertNotNull(deviceInfo);
    assertEquals(locationStatus.getLocation().getLocationId(), deviceInfo.getLocationId());
    assertEquals(locationStatus.getStatus(), deviceInfo.getStatus());
    assertNull(locationStatus.getContainer());

    // validate
    validator.validate(deviceInfo);
  }

  private LocationStatus loadDeviceInfo(String file) throws IOException
  {
    InputStream inputStream = TypeReference.class.getResourceAsStream(file);
    return objectMapper.readValue(inputStream, LocationStatus.class);
  }

}
