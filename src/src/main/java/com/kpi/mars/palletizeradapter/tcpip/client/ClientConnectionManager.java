/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter.tcpip.client;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a manager of {@link PalletizerClientConnection}s which simply starts and stops them using
 * {@link SmartLifecycle}.
 *
 * @author sja
 */
@Component
public class ClientConnectionManager implements SmartLifecycle
{
  private final Map<String, PalletizerClientConnection> clientConnections;
  private final ReentrantLock                           lifeCycleLock;

  private boolean isRunning;

  /**
   * Creates a new instance of the {@link ClientConnectionManager} class.
   */
  public ClientConnectionManager(Map<String, PalletizerClientConnection> clientConnections)
  {
    this.clientConnections = clientConnections;
    this.lifeCycleLock = new ReentrantLock();
  }

  @Override
  public void start()
  {
    lifeCycleLock.lock();
    try
    {
      if (!isRunning())
      {
        isRunning = true;
        // start client connections
        for (PalletizerClientConnection clientConnection : clientConnections.values())
        {
          clientConnection.start();
        }
      }
    }
    finally
    {
      lifeCycleLock.unlock();
    }
  }

  @Override
  public void stop()
  {

    lifeCycleLock.lock();
    try
    {
      if (isRunning())
      {
        isRunning = false;
        // stop client connections
        for (PalletizerClientConnection clientConnection : clientConnections.values())
        {
          clientConnection.stop();
        }
      }
    }
    finally
    {
      lifeCycleLock.unlock();
    }
  }

  @Override
  public boolean isRunning()
  {
    return isRunning;
  }
}
