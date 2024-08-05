/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2023 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpi.roboticshub.adapter.mongo.ActivityService;
import com.kpi.roboticshub.adapter.mongo.MessageService;
import com.kpi.roboticshub.adapter.mongo.entity.ActivityWrapper;
import com.kpi.roboticshub.api.Activity;
import com.kpi.roboticshub.api.Response;
import com.kpi.sol.tcpip.properties.AdapterConnectionProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Represents a {@link PalletizerActivityService} test.
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PalletizerActivityServiceTest
{
  @Autowired
  private ObjectMapper                objectMapper;
  @Autowired
  private ActivityService             activityService;
  @Autowired
  private MessageService              messageService;
  @Autowired
  private PalletizerActivityService   palletizerActivityService;
  @Autowired
  private PalletizerAdapterProperties properties;
  private List<Socket>                sockets;
  private List<ServerSocket>          serverSockets;

  @BeforeAll
  public void start() throws IOException
  {
    sockets = new ArrayList<>();
    serverSockets = new ArrayList<>();
    for (AdapterConnectionProperties connectionProperties : properties.getClientConnections().values())
    {
      var serverSocket = new ServerSocket(connectionProperties.getPort());
      serverSockets.add(serverSocket);
      sockets.add(serverSocket.accept());
    }
    for (Socket socket : sockets)
    {
      await().atLeast(Duration.ofMillis(50))
          .untilAsserted(() -> assertTrue(socket.isConnected()));
    }
  }

  @AfterAll
  public void stop() throws IOException
  {
    for (Socket socket : sockets)
    {
      socket.close();
    }
    for (ServerSocket serverSocket : serverSockets)
    {
      serverSocket.close();
    }
    // clear mongo
    activityService.getRepository().deleteAll();
    messageService.getRepository().deleteAll();
  }

  @Test
  void processActivity() throws IOException
  {
    Activity activity = loadActivity("/sample-messages/from-mars-client/build-activity.json");
    Response<Activity> activityResponse = palletizerActivityService.processActivity(activity);
    Optional<ActivityWrapper> activityEntity = activityService.getRepository()
        .findById(activity.getActivityDetail().getActivityId());
    if (activityEntity.isPresent())
    {
      assertNotNull(activityEntity.get());
      assertEquals(activityEntity.get().getDataObject().getActivityType(), activity.getActivityType());
      assertEquals(activityEntity.get().getDataObject().getDeviceType(), activity.getDeviceType());
      assertEquals(activityEntity.get().getDataObject().getSystemId(), activity.getSystemId());
      assertEquals(activityEntity.get().getDataObject().getActivityDetail().getActivityId(),
                   activity.getActivityDetail().getActivityId());
      assertEquals(HttpStatus.OK, activityEntity.get().getStatus());
    }
    assertNotNull(activityResponse);
    assertEquals(activityResponse.getMessage().getActivityType(), activity.getActivityType());
    assertEquals(activityResponse.getMessage().getDeviceType(), activity.getDeviceType());
    assertEquals(activityResponse.getMessage().getSystemId(), activity.getSystemId());
    assertEquals(activityResponse.getMessage().getActivityDetail().getActivityId(),
                 activity.getActivityDetail().getActivityId());
  }

  public Activity loadActivity(String path) throws IOException
  {
    return objectMapper.readValue(TypeReference.class.getResourceAsStream(path), getActivityReference());
  }

  protected TypeReference<Activity> getActivityReference()
  {
    return new TypeReference<>()
    {
    };
  }
}
