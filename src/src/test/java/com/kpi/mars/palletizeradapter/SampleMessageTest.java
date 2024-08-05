/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpi.mars.palletizer.api.BuildComplete;
import com.kpi.mars.palletizer.api.BuildRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Represents a test to load the sample JSON messages.
 *
 * @author sja
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SampleMessageTest
{
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void parseBuildRequestOne() throws IOException
  {
    final String path = "src/test/resources/sample-messages/to-palletizer/build-request.json";
    BuildRequest buildRequest = objectMapper.readValue(loadResource(path), BuildRequest.class);
    assertNotNull(buildRequest);
    assertNotNull(buildRequest.getContainer());
    assertNotNull(buildRequest.getContainer().getContainerId());
    assertNotNull(buildRequest.getContainer().getLength());
  }

  @Test
  public void parseBuildRequestTwo() throws IOException
  {
    final String path = "src/test/resources/sample-messages/to-palletizer/bryon-build-request.json";
    BuildRequest buildRequest = objectMapper.readValue(loadResource(path), BuildRequest.class);
    assertNotNull(buildRequest);
    assertNotNull(buildRequest.getContainer());
    assertNotNull(buildRequest.getContainer().getContainerId());
    assertNull(buildRequest.getContainer().getLength());
    assertNotNull(buildRequest.getSource());
    assertNotNull(buildRequest.getDestination());
  }

  @Test
  public void parseBuildCompleteOne() throws IOException
  {
    final String path = "src/test/resources/sample-messages/from-palletizer/build-complete.json";
    BuildComplete buildComplete = objectMapper.readValue(loadResource(path), BuildComplete.class);
    assertNotNull(buildComplete);
    assertNotNull(buildComplete.getStatus());
    assertNotNull(buildComplete.getContainer());
    assertNotNull(buildComplete.getContainer().getContainerId());
    assertNotNull(buildComplete.getContainer().getLength());
    assertNotNull(buildComplete.getDestination());
    assertNotNull(buildComplete.getDestination().getLocationId());
    assertNotNull(buildComplete.getDestination().getX());
  }

  @Test
  public void parseBuildCompleteTwo() throws IOException
  {
    final String path = "src/test/resources/sample-messages/from-palletizer/bryon-build-complete.json";
    BuildComplete buildComplete = objectMapper.readValue(loadResource(path), BuildComplete.class);
    assertNotNull(buildComplete);
    assertNotNull(buildComplete.getStatus());
    assertNotNull(buildComplete.getContainer());
    assertNotNull(buildComplete.getContainer().getContainerId());
    assertNull(buildComplete.getContainer().getLength());
    assertNotNull(buildComplete.getDestination());
    assertNotNull(buildComplete.getDestination().getLocationId());
    assertNull(buildComplete.getDestination().getX());
  }

  private File loadResource(String path)
  {
    File file = new File(path);
    assertTrue(file.exists());
    return file;
  }
}
