package com.kpi.mars.palletizeradapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpi.mars.palletizer.api.BuildComplete;
import com.kpi.mars.palletizeradapter.converter.BuildCompleteConverter;
import com.kpi.mars.palletizeradapter.validator.BuildCompleteValidator;
import com.kpi.roboticshub.adapter.AdapterProperties;
import com.kpi.roboticshub.adapter.mongo.SubscriptionRepository;
import com.kpi.roboticshub.adapter.mongo.entity.Subscription;
import com.kpi.roboticshub.adapter.mongo.entity.SubscriptionType;
import com.kpi.roboticshub.adapter.validation.ValidationException;
import com.kpi.roboticshub.api.Container;
import com.kpi.roboticshub.api.DeviceEvent;
import com.kpi.roboticshub.api.Location;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BuildCompleteTest
{
  @Autowired
  private ObjectMapper           objectMapper;
  @Autowired
  private BuildCompleteConverter converter;
  @Autowired
  private BuildCompleteValidator validator;
  @Autowired
  private SubscriptionRepository subscriptionRepository;
  @Autowired
  private AdapterProperties      adapterProperties;

  @BeforeAll
  public void start()
  {
    Subscription createSubscription = new Subscription();
    createSubscription.setId("123");
    createSubscription.setType(SubscriptionType.DEVICE_EVENT);
    createSubscription.setUrl(URI.create("http://localhost:45001/palletizer-client-controller/sendClientMessage"));
    subscriptionRepository.save(createSubscription);
  }

  @AfterAll
  public void stop()
  {
    subscriptionRepository.deleteAll();
  }

  @Test
  void convertInvalidBuildComplete()
  {
    BuildComplete buildComplete = BuildComplete.builder().build();
    DeviceEvent deviceEvent = converter.convert(buildComplete);
    Assertions.assertThrows(ValidationException.class, () -> validator.validate(deviceEvent));
  }

  @Test
  void convertAndValidateBuildComplete() throws IOException
  {
    BuildComplete buildComplete = loadBuildComplete("/sample-messages/from-palletizer/build-complete.json");
    DeviceEvent deviceEvent = converter.convert(buildComplete);
    assertNotNull(deviceEvent);
    assertEquals(buildComplete.getDeviceId(), deviceEvent.getDeviceId());
    assertEquals(buildComplete.getStatus(), deviceEvent.getActivityState());
    assertEquals(buildComplete.getContainer().getContainerId(),
                 deviceEvent.getContainer().get(0).getContainerId());
    assertEquals(buildComplete.getDestination().getLocationId(), deviceEvent.getLocation().getLocationId());
    assertNull(deviceEvent.getErrorCode());
    validator.validate(deviceEvent);
  }

  @Test
  void convertAndValidateBuildCompleteFailure() throws IOException
  {
    BuildComplete buildComplete = loadBuildComplete("/sample-messages/from-palletizer/build-complete-failure.json");
    DeviceEvent deviceEvent = converter.convert(buildComplete);
    assertNotNull(deviceEvent);
    assertEquals(buildComplete.getDeviceId(), deviceEvent.getDeviceId());
    assertEquals(buildComplete.getStatus(), deviceEvent.getActivityState());
    assertEquals(buildComplete.getContainer().getContainerId(),
                 deviceEvent.getContainer().get(0).getContainerId());
    assertEquals(buildComplete.getDestination().getLocationId(), deviceEvent.getLocation().getLocationId());
    assertEquals(buildComplete.getStatus(), deviceEvent.getErrorCode());
    validator.validate(deviceEvent);
  }

  @Test
  void deviceEventValidatorTest() throws IOException
  {
    BuildComplete buildComplete = loadBuildComplete("/sample-messages/from-palletizer/build-complete.json");
    assertNotNull(buildComplete);
    Location destination = Location.builder()
        .locationId(buildComplete.getDestination().getLocationId())
        .build();
    Container container = Container.builder()
        .containerId(buildComplete.getContainer().getContainerId())
        .location(destination)
        .build();
    DeviceEvent deviceEvent = DeviceEvent.builder()
        .systemId(adapterProperties.getSystemId())
        .deviceId(buildComplete.getDeviceId())
        .activityState(buildComplete.getStatus())
        .activityId(buildComplete.getBuildId())
        .container(List.of(container))
        .location(destination)
        .timestamp(OffsetDateTime.now())
        .build();
    validator.validate(deviceEvent);
  }

  public BuildComplete loadBuildComplete(String path) throws IOException
  {
    InputStream inputStream = TypeReference.class.getResourceAsStream(path);
    return objectMapper.readValue(inputStream, getReference());
  }

  protected TypeReference<BuildComplete> getReference()
  {
    return new TypeReference<>()
    {
    };
  }
}
