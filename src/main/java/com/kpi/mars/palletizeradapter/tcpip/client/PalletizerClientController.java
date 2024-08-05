/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.tcpip.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kpi.mars.common.api.BaseMessage;
import com.kpi.mars.palletizeradapter.services.BaseMessageService;
import com.kpi.mars.palletizeradapter.util.MemoryUtil;
import com.kpi.roboticshub.api.Response;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a {@link RestController} that allows for manual {@link PalletizerClientConnection} interaction for
 * debugging, testing or support.
 *
 * @author sja
 */
@RestController
@RequestMapping(value = "tcp-ip-client",
    produces = { MediaType.APPLICATION_JSON_VALUE },
    consumes = { MediaType.APPLICATION_JSON_VALUE })
public class PalletizerClientController
{
  private final Map<String, PalletizerClientConnection> clientConnections;
  private final BaseMessageService                      baseMessageService;

  /**
   * Creates a new instance of the {@link PalletizerClientController} class.
   */
  public PalletizerClientController(Map<String, PalletizerClientConnection> clientConnections,
                                    BaseMessageService baseMessageService)
  {
    this.clientConnections = clientConnections;
    this.baseMessageService = baseMessageService;
  }

  /**
   * Closes the {@link PalletizerClientConnection} assocaited with the specified {@code deviceId}.
   *
   * @return a confirmation that the socket was closed.
   */
  @PostMapping(value = "close-client-connection", consumes = { MediaType.ALL_VALUE })
  public Object closeClientConnections(@RequestParam(required = false) String deviceId)
  {
    PalletizerClientConnection clientConnection = getClientConnection(deviceId);
    if (clientConnection != null)
    {
      clientConnection.close();
      return Map.of("result", "Client connection closed");
    }
    else
    {
      return Map.of("result", "Client connection [%s] does not exist.".formatted(deviceId));
    }
  }

  /**
   * Sends a {@link BaseMessage} via the {@link PalletizerClientConnection}.
   *
   * @param message the {@link BaseMessage} to send.
   * @return a confirmation that the {@link BaseMessage} was sent.
   * @throws JsonProcessingException when the {@code message} cannot be parsed.
   */
  @PostMapping("send-client-message")
  public Object sendClientMessage(@RequestParam(required = false) String deviceId, @RequestBody String message)
      throws JsonProcessingException
  {
    PalletizerClientConnection clientConnection = getClientConnection(deviceId);
    if (clientConnection != null)
    {
      BaseMessage baseMessage = baseMessageService.getBaseMessage(message);
      clientConnection.write(baseMessage, true);
      return Map.of("result", "Message sent");
    }
    else
    {
      return Map.of("result", "Client connection [%s] does not exist.".formatted(deviceId));
    }
  }

  /**
   * Gets the statistics for all connections or a specific connection.
   *
   * @param deviceId the specific connection to get statistics for. No value defaults to all connections.
   * @return an {@link Object} containing connection statistics.
   */
  @GetMapping(value = "get-statistics", consumes = { MediaType.ALL_VALUE })
  public Object getStatistics(@RequestParam(required = false) String deviceId)
  {
    Map<String, Object> statistics = new HashMap<>();
    if (Objects.equals(deviceId, null))
    {
      // add all server socket statistics
      for (Map.Entry<String, PalletizerClientConnection> entry : clientConnections.entrySet())
      {
        statistics.put(entry.getKey(), entry.getValue().getStatistics());
      }
    }
    else
    {
      // add single server socket statistic
      PalletizerClientConnection clientConnection = clientConnections.get(deviceId);
      if (clientConnection != null)
      {
        statistics.put(deviceId, clientConnection.getStatistics());
      }
    }
    return statistics;
  }

  @GetMapping(value = "memoryUsage", consumes = { MediaType.ALL_VALUE })
  public Response<Map<Object,Object>> getSystemUsage()
  {
    return MemoryUtil.getSystemMemoryUsage();
  }

  private PalletizerClientConnection getClientConnection(String deviceId)
  {
    PalletizerClientConnection clientConnection;
    if (deviceId == null)
    {
      clientConnection = clientConnections.values().stream().findFirst().orElse(null);
    }
    else
    {
      clientConnection = clientConnections.get(deviceId);
    }
    return clientConnection;
  }
}
