/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2023 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kpi.roboticshub.api.Response;
import com.kpi.sol.api.AuthenticationRequest;
import com.kpi.sol.api.TokenResponse;
import com.kpi.sol.keycloak.KeycloakService;
import com.kpi.sol.tcpip.properties.AdapterConnectionProperties;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Represents a security test
 *
 * @author KarnakarChitikaneni
 */
@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
class SecurityTest
{
  @Autowired
  private KeycloakService             keycloakService;
  @Autowired
  private MockMvc                     mockMvc;
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
  }

  String obtainAccessToken(AuthenticationRequest authenticationRequest)
  {
    ResponseEntity<Response<Object>> tokenResponse = keycloakService.login(authenticationRequest);
    assertNotNull(tokenResponse);
    assertNotNull(tokenResponse.getBody());
    return ((TokenResponse) tokenResponse.getBody().getMessage()).getAccessToken();
  }

  String obtainAccessForClientCredentials()
  {
    ResponseEntity<Response<Object>> tokenResponse = keycloakService.getTokenForClientCredentials();
    assertNotNull(tokenResponse);
    assertNotNull(tokenResponse.getBody());
    return ((TokenResponse) tokenResponse.getBody().getMessage()).getAccessToken();
  }

  @Test
  @Disabled
  void givenValidCredentials_whenPostProtectedResource_thenAuthorized() throws Exception
  {
    String accessToken = obtainAccessForClientCredentials();
    mockMvc.perform(post("/mars/device-activity")
                        .content(TypeReference.class.getResourceAsStream
                            ("/sample-messages/from-mars-client/build-activity.json").readAllBytes())
                        .contentType(APPLICATION_JSON)
                        .headers(getHeaders(accessToken)))
        .andExpect(status().isOk());
  }

  @Test
  @Disabled("Broken login")
  void givenInvalidCredentials_whenPostProtectedRequest_thenForbidden() throws Exception
  {
    String accessToken = obtainAccessToken(AuthenticationRequest.builder().username("robo2")
                                               .password("password").build());
    mockMvc.perform(post("/mars/device-activity")
                        .content(TypeReference.class.getResourceAsStream
                            ("/sample-messages/from-mars-client/build-activity.json").readAllBytes())
                        .contentType(APPLICATION_JSON)
                        .headers(getHeaders(accessToken)))
        .andExpect(status().isForbidden());
  }

  @Test
  @Disabled
  void givenInvalidToken_whenPostProtectedRequest_thenUnauthorized() throws Exception
  {
    mockMvc.perform(post("/mars/device-activity")
                        .content(TypeReference.class.getResourceAsStream
                            ("/sample-messages/from-mars-client/build-activity.json").readAllBytes())
                        .contentType(APPLICATION_JSON)
                        .headers(getHeaders("some-thing-random")))
        .andExpect(status().isUnauthorized());
  }

  HttpHeaders getHeaders(String token)
  {
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return headers;
  }
}
