/* ***************************************************************************************
 * (c) COPYRIGHT Kuecker Pulse Integration, L.P. 2022 All Rights Reserved
 * No part of this copyrighted work may be reproduced, modified, or distributed
 * in any form or by any means without the prior written permission of Kuecker Pulse
 * Integration, L.P.
 ****************************************************************************************/
package com.kpi.mars.palletizeradapter;

import brave.sampler.Sampler;
import com.kpi.mars.palletizer.api.BuildComplete;
import com.kpi.mars.palletizer.api.BuildReleaseComplete;
import com.kpi.mars.palletizer.api.LocationStatus;
import com.kpi.mars.palletizeradapter.converter.BuildCompleteConverter;
import com.kpi.mars.palletizeradapter.converter.BuildReleaseCompleteConverter;
import com.kpi.mars.palletizeradapter.converter.LocationStatusConverter;
import com.kpi.mars.palletizeradapter.services.BaseMessageService;
import com.kpi.mars.palletizeradapter.tcpip.client.PalletizerClientConnection;
import com.kpi.mars.palletizeradapter.tcpip.client.PalletizerReceiveService;
import com.kpi.mars.palletizeradapter.tcpip.client.PalletizerSendMessageHandler;
import com.kpi.roboticshub.adapter.ClientSendMessageHandler;
import com.kpi.roboticshub.adapter.ConversionService;
import com.kpi.roboticshub.adapter.controller.MarsContainerController;
import com.kpi.roboticshub.api.Activity;
import com.kpi.roboticshub.api.DeviceEvent;
import com.kpi.roboticshub.api.DeviceInfo;
import com.kpi.roboticshub.api.DeviceRequest;
import com.kpi.sol.keycloak.SecurityProperties;
import com.kpi.sol.tcpip.properties.AdapterConnectionProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import reactor.netty.http.client.HttpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a {@link PalletizerAdapterApplication} configuration.
 *
 * @author sja
 */
@Configuration
@ComponentScan(basePackages = { "com.kpi.roboticshub.adapter", "com.kpi.sol" }, excludeFilters = { @ComponentScan.Filter
                                                                                                       (type = FilterType.ASSIGNABLE_TYPE,
                                                                                                           classes = {
                                                                                                               MarsContainerController.class }) })
@EnableConfigurationProperties({ PalletizerAdapterProperties.class, PalletizerValidatorProperties.class,
                                 SecurityProperties.class })
@EnableRetry
@EnableTransactionManagement
@EnableMongoRepositories(basePackages = { "com.kpi.roboticshub.adapter.mongo" })
public class PalletizerAdapterConfiguration
{
  private static final Logger LOGGER = LogManager.getLogger();

  private final SecurityProperties securityProperties;

  public PalletizerAdapterConfiguration(SecurityProperties securityProperties)
  {
    this.securityProperties = securityProperties;
  }

  /**
   * The {@link HttpClient} to use.
   *
   * @see HttpClient
   */
  @Bean
  @ConditionalOnMissingBean
  public HttpClient httpClient()
  {
    return HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
        .doOnConnected(c -> c.addHandlerFirst(new ReadTimeoutHandler(15))
            .addHandlerFirst(new WriteTimeoutHandler(15)));
  }

  /**
   * The {@link OpenAPI} configuration.
   *
   * @see OpenAPI
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenAPI openApi()
  {
    OpenAPI openAPI = new OpenAPI()
        .info(new Info().title("Palletizer Adapter API")
                  .description("")
                  .contact(new Contact().name("KPI Solutions, LLC").url("https://kpisolutions.com/"))
                  .version("v1.0.0"))
        .externalDocs(new ExternalDocumentation()
                          .description("Palletizer Interface Specification")
                          .url("https://kpisolutionsinc.sharepoint.com/:w:/r/sites/Sol" +
                               "/Shared%20Documents/General/Mars%20(Robotics%20Hub)" +
                               "/Standard%20Interface%20Specifications" +
                               "/Palletizer%20Interface%20Specification.docx" +
                               "?d=w12487228ca5b48949707de7bcfd114b5&csf=1&web=1&e=BwtN6T"));
    final String securitySchemeName = "bearerAuth";
    if (securityProperties.isSecurityEnabled() && securityProperties.getAccessDecisionMapType() != null)
    {
      openAPI.components(new Components().addSecuritySchemes(securitySchemeName,
                                                             new SecurityScheme()
                                                                 .type(SecurityScheme.Type.HTTP)
                                                                 .scheme("bearer")
                                                                 .bearerFormat("JWT")))
          .security(List.of(new SecurityRequirement().addList(securitySchemeName)));
    }
    return openAPI;
  }

  /**
   * The {@link PalletizerClientConnection}s being managed by this instance.
   * <p>
   * Keyed by device ID.
   */
  @Bean
  public Map<String, PalletizerClientConnection> clientConnections(PalletizerAdapterProperties properties,
                                                                   PalletizerReceiveService receiveService,
                                                                   BaseMessageService baseMessageService)
  {
    Map<String, PalletizerClientConnection> clientConnections = new HashMap<>();
    for (Map.Entry<String, AdapterConnectionProperties> entry : properties.getClientConnections().entrySet())
    {
      var clientConnection = new PalletizerClientConnection(receiveService, entry.getKey(), entry.getValue(),
                                                            baseMessageService);
      clientConnections.put(entry.getKey(), clientConnection);
    }
    return clientConnections;
  }

  /**
   * The {@link SubscribableChannel} that all messages received from RH clients are routed to.
   */
  @Bean
  public SubscribableChannel adapterReceiveChannel()
  {
    return MessageChannels.direct("adapterReceiveChannel").get();
  }

  /**
   * The {@link SubscribableChannel} that all messages received from palletizers are routed to then sent to the RH
   * clients.
   */
  @Bean
  public SubscribableChannel adapterSendChannel()
  {
    return MessageChannels.direct("adapterSendChannel").get();
  }

  /**
   * The {@link IntegrationFlow} that processes messages received from RH clients by validating and sending to a
   * palletizer.
   */
  @Bean
  public IntegrationFlow adapterReceiveFlow(SubscribableChannel adapterReceiveChannel,
                                            PalletizerSendMessageHandler sendMessageHandler,
                                            ConversionService conversionService)
  {
    return IntegrationFlows.from(adapterReceiveChannel)
        // validate
        .filter(x -> {
          if (x instanceof Activity activity)
          {
            conversionService.validate(activity);
          }
          else if (x instanceof DeviceRequest deviceRequest)
          {
            conversionService.validate(deviceRequest);
          }
          return true;
        })
        .transform(source -> {
          if (source instanceof Activity activity)
          {
            return conversionService.convert(activity);
          }
          else if (source instanceof DeviceRequest deviceRequest)
          {
            return conversionService.convert(deviceRequest);
          }
          return source;
        })
        // send
        .handle(sendMessageHandler)
        .get();
  }

  /**
   * The {@link IntegrationFlow} that processes messages received from palletizers by validating and sending to RH
   * clients.
   */
  @Bean
  public IntegrationFlow adapterSendFlow(SubscribableChannel adapterSendChannel,
                                         ClientSendMessageHandler clientSendMessageHandler,
                                         ConversionService conversionService,
                                         BuildCompleteConverter buildCompleteConverter,
                                         BuildReleaseCompleteConverter buildReleaseCompleteConverter,
                                         LocationStatusConverter locationStatusConverter)
  {
    return IntegrationFlows.from(adapterSendChannel)
        // transform
        .transform(source -> {
          if (source instanceof BuildComplete buildComplete)
          {

            return buildCompleteConverter.convert(buildComplete);
          }
          else if (source instanceof BuildReleaseComplete buildReleaseComplete)
          {

            return buildReleaseCompleteConverter.convert(buildReleaseComplete);
          }
          else if (source instanceof LocationStatus locationStatus)
          {
            return locationStatusConverter.convert(locationStatus);
          }
          return source;
        })
        // validate
        .filter(x -> {
          if (x instanceof DeviceEvent deviceEvent)
          {
            try
            {
              conversionService.validate(deviceEvent);
            }
            catch (ClassNotFoundException e)
            {
              LOGGER.error("Unable to validate message:\n{}\nException:", deviceEvent);
            }
            return true;
          }
          if (x instanceof DeviceInfo deviceInfo)
          {
            try
            {
              conversionService.validate(deviceInfo);
            }
            catch (ClassNotFoundException e)
            {
              LOGGER.error("Unable to validate message:\n{}\nException:", deviceInfo);
            }
            return true;
          }
          return false;
        })
        // send
        .handle(clientSendMessageHandler)
        .get();
  }

    @Bean
    public Sampler defaultSampler()
    {
      return Sampler.ALWAYS_SAMPLE;
    }

}
