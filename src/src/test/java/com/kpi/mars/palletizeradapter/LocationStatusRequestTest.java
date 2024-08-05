/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2023 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpi.mars.common.api.BaseMessage;
import com.kpi.mars.palletizer.api.LocationStatusRequest;
import com.kpi.mars.palletizeradapter.converter.LocationStatusRequestConverter;
import com.kpi.mars.palletizeradapter.validator.LocationStatusRequestValidator;
import com.kpi.roboticshub.api.DeviceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocationStatusRequestTest
{
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private LocationStatusRequestConverter converter;

  @Autowired
  private LocationStatusRequestValidator validator;

  @Test
  void convertValidRequestTestOne() throws IOException
  {
    DeviceRequest deviceRequest = loadDeviceRequest("/sample-messages/from-mars-client/location-status-request-1.json");
    //validate
    validator.validate(deviceRequest);
    // convert
    BaseMessage message = converter.convert(deviceRequest);
    LocationStatusRequest locationStatusRequest = (LocationStatusRequest) message;
    assertNotNull(message);
    assertEquals(deviceRequest.getRequestType(), message.getMessageType());
  }

  @Test
  void convertValuesWithNullTestTwo() throws IOException
  {
    BaseMessage baseMessage = converter.convert(null);
    assertNull(baseMessage);
  }

  @Test
  void convertValuesWithTestThree() throws IOException
  {
    DeviceRequest deviceRequest = loadDeviceRequest("/sample-messages/from-mars-client/location-status-request-2.json");
    //validate
    validator.validate(deviceRequest);

    // convert
    BaseMessage message = converter.convert(deviceRequest);
    LocationStatusRequest locationStatusRequest = (LocationStatusRequest) message;
    assertNotNull(message);
    assertEquals(deviceRequest.getRequestType(), message.getMessageType());
  }

  private DeviceRequest loadDeviceRequest(String file) throws IOException
  {
    InputStream inputStream = TypeReference.class.getResourceAsStream(file);
    return objectMapper.readValue(inputStream, DeviceRequest.class);
  }

}
