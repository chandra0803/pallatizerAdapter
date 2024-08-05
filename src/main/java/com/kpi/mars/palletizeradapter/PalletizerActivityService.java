/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import com.kpi.roboticshub.adapter.mongo.ActivityService;
import com.kpi.roboticshub.adapter.validation.ValidationException;
import com.kpi.roboticshub.api.Activity;
import com.kpi.roboticshub.api.ApiError;
import com.kpi.roboticshub.api.ApiErrorConstants;
import com.kpi.roboticshub.api.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents a {@link Service} to process the message.
 *
 * @author PravinVerma
 */
@Service
public class PalletizerActivityService
{
  private static final Logger LOGGER = LogManager.getLogger();

  private final ActivityService activityService;
  private final MessageChannel  adapterReceiveChannel;

  /**
   * Creates a new instance of the {@link PalletizerActivityService} class.
   */
  public PalletizerActivityService(ActivityService activityService, MessageChannel adapterReceiveChannel)
  {
    this.activityService = activityService;
    this.adapterReceiveChannel = adapterReceiveChannel;
  }

  /**
   * Process the {@link Activity} message.
   */
  public Response<Activity> processActivity(Activity activity)
  {
    try
    {
      adapterReceiveChannel.send(MessageBuilder.withPayload(activity).build());
    }
    catch (MessageHandlingException me)
    {
      LOGGER.error("Unable to process activity. Exception:", me);
      ClientException clientException;
      if (me.getCause() instanceof ValidationException ve)
      {
        throw ve;
      }
      else if (me.getCause() instanceof ClientException ce)
      {
        throw ce;
      }
      else
      {
        ApiError apiError = ApiError.builder().errorId(ApiErrorConstants.UNEXPECTED_FAILURE_ID)
            .description(ApiErrorConstants.UNEXPECTED_FAILURE_FORMAT.formatted(me.getMessage()))
            .build();
        clientException = new ClientException(activity, List.of(apiError), HttpStatus.INTERNAL_SERVER_ERROR);
      }
      throw clientException;
    }
    activityService.save(activity.getActivityDetail().getActivityId(),
                         OffsetDateTime.now(), activity, HttpStatus.OK, null);
    return Response.<Activity>builder().message(activity).build();
  }
}
