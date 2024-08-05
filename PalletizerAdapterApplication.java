/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import org.apache.logging.log4j.LogManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import static java.lang.System.exit;
import static java.lang.System.out;

/**
 * Represents the main application starting point.
 */
@SpringBootApplication
@RefreshScope
public class PalletizerAdapterApplication
{
  public static void main(String[] args)
  {
    // log uncaught exceptions
    Thread.setDefaultUncaughtExceptionHandler(new ApplicationExceptionHandler());
    try
    {
      SpringApplication.run(PalletizerAdapterApplication.class, args);
    }
    catch (Throwable ex)
    {
      final String message = "Uncaught Spring Application Exception:";
      LogManager.getLogger().fatal(message, ex);
      // log4j loggers may not be initialized yet
      out.println(message);
      ex.printStackTrace();
      exit(ex.hashCode());
    }
  }

  static class ApplicationExceptionHandler implements Thread.UncaughtExceptionHandler
  {
    public void uncaughtException(Thread thread, Throwable ex)
    {
      final String message = String.format("Uncaught exception on thread [%s]. Exception:", thread);
      LogManager.getLogger().fatal(message, ex);
      // log4j loggers may not be initialized yet
      out.println(message);
      ex.printStackTrace();
    }
  }
}
