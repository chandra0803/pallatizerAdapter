/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2023 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpi.mars.palletizer.api.BuildRequest;
import com.kpi.mars.palletizeradapter.converter.BuildRequestConverter;
import com.kpi.roboticshub.api.Activity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class BuildRequestTest
{
  @Autowired
  private ObjectMapper          objectMapper;
  @Autowired
  private BuildRequestConverter converter;

  @Test
  void testBuildRequestConverter() throws IOException
  {
    Activity activityRequest = createActivityRequest("/sample-messages/from-mars-client/build-activity.json");
    BuildRequest buildRequest = converter.convert(activityRequest);
    assertNotNull(buildRequest);
    BuildRequest buildRequestSample = createBuildRequest("/sample-messages/to-palletizer/build-request.json");
    assertEquals(buildRequest.getDeviceId(), buildRequestSample.getDeviceId());
    assertEquals(buildRequest.getMessageType(), buildRequestSample.getMessageType());
    assertEquals(buildRequest.getContainer().getContainerId(), buildRequestSample.getContainer().getContainerId());
    assertEquals(buildRequest.getSource().getLocationId(), buildRequestSample.getSource().getLocationId());
    assertEquals(buildRequest.getDestination().getLocationId(), buildRequestSample.getDestination().getLocationId());
  }

  @Test
  void testBryonBuildRequestConverter() throws IOException
  {
    Activity activityRequest = createActivityRequest("/sample-messages/from-mars-client/bryon-build-activity.json");
    BuildRequest buildRequest = converter.convert(activityRequest);
    assertNotNull(buildRequest);
    BuildRequest buildRequestSample = createBuildRequest("/sample-messages/to-palletizer/bryon-build-request.json");
    assertEquals(buildRequest.getDeviceId(), buildRequestSample.getDeviceId());
    assertEquals(buildRequest.getMessageType(), buildRequestSample.getMessageType());
    assertEquals(buildRequest.getContainer().getContainerId(), buildRequestSample.getContainer().getContainerId());
    assertEquals(buildRequest.getSource().getLocationId(), buildRequestSample.getSource().getLocationId());
    assertEquals(buildRequest.getDestination().getLocationId(), buildRequestSample.getDestination().getLocationId());
  }

  public Activity createActivityRequest(String path) throws IOException
  {
    InputStream inputStream = TypeReference.class.getResourceAsStream(path);
    return objectMapper.readValue(inputStream, getActivityReference());
  }

  public BuildRequest createBuildRequest(String path) throws IOException
  {
    InputStream inputStream = TypeReference.class.getResourceAsStream(path);
    return objectMapper.readValue(inputStream, getBuildRequestReference());
  }

  protected TypeReference<Activity> getActivityReference()
  {
    return new TypeReference<>()
    {
    };
  }

  protected TypeReference<BuildRequest> getBuildRequestReference()
  {
    return new TypeReference<>()
    {
    };
  }
}
