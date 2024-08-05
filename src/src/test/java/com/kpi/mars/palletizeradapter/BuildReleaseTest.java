/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2023 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpi.mars.palletizer.api.BuildRelease;
import com.kpi.mars.palletizeradapter.converter.BuildReleaseConverter;
import com.kpi.roboticshub.api.Activity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class BuildReleaseTest
{
  @Autowired
  private ObjectMapper          objectMapper;
  @Autowired
  private BuildReleaseConverter converter;

  @Test
  void testBuildReleaseConverter() throws IOException
  {
    Activity activityRequest = createActivityRequest("/sample-messages/from-mars-client/build-release.json");
    BuildRelease buildRelease = converter.convert(activityRequest);
    assertNotNull(buildRelease);
    BuildRelease buildReleaseSample = createBuildRelease("/sample-messages/to-palletizer/build-release.json");
    assertEquals(buildRelease.getDeviceId(), buildReleaseSample.getDeviceId());
    assertEquals(buildRelease.getMessageType(), buildReleaseSample.getMessageType());
    assertEquals(buildRelease.getSource().getLocationId(), buildReleaseSample.getSource().getLocationId());
  }

  public Activity createActivityRequest(String path) throws IOException
  {
    InputStream inputStream = TypeReference.class.getResourceAsStream(path);
    return objectMapper.readValue(inputStream, getActivityReference());
  }

  public BuildRelease createBuildRelease(String path) throws IOException
  {
    InputStream inputStream = TypeReference.class.getResourceAsStream(path);
    return objectMapper.readValue(inputStream, getBuildReleaseReference());
  }

  protected TypeReference<Activity> getActivityReference()
  {
    return new TypeReference<>()
    {
    };
  }

  protected TypeReference<BuildRelease> getBuildReleaseReference()
  {
    return new TypeReference<>()
    {
    };
  }
}
