package com.kpi.mars.palletizeradapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpi.mars.palletizer.api.BuildReleaseComplete;
import com.kpi.mars.palletizeradapter.converter.BuildReleaseCompleteConverter;
import com.kpi.mars.palletizeradapter.validator.BuildCompleteValidator;
import com.kpi.roboticshub.adapter.AdapterProperties;
import com.kpi.roboticshub.adapter.mongo.SubscriptionRepository;
import com.kpi.roboticshub.adapter.mongo.entity.Subscription;
import com.kpi.roboticshub.adapter.mongo.entity.SubscriptionType;
import com.kpi.roboticshub.api.DeviceEvent;
import com.kpi.roboticshub.api.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BuildReleaseCompleteTest
{
  @Autowired
  private ObjectMapper                  objectMapper;
  @Autowired
  private BuildReleaseCompleteConverter converter;
  @Autowired
  private BuildCompleteValidator        validator;
  @Autowired
  private SubscriptionRepository        subscriptionRepository;
  @Autowired
  private AdapterProperties             properties;

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
  void validBuildReleaseCompleteTest() throws IOException
  {
    Subscription subscription = new Subscription();
    subscription.setId("123");
    Optional<Subscription> subscriptionForDeviceId = subscriptionRepository.findById(subscription.getId());

    if (subscriptionForDeviceId.isPresent())
    {
      BuildReleaseComplete buildReleaseComplete = loadBuildReleaseComplete();
      buildReleaseComplete.setMessageNumber(new Random().nextInt(1000));
      buildReleaseComplete.setMessageType("BuildReleaseComplete");
      buildReleaseComplete.setRequestNumber(Integer.parseInt(subscriptionForDeviceId.get().getId()));
      assertNotNull(converter.convert(buildReleaseComplete));
    }
    else
    {
      fail("No device event from subscription");
    }
  }

  @Test
  void InValidBuildCompleteTest() throws IOException
  {
    Subscription subscription = new Subscription();
    subscription.setId("123");
    Optional<Subscription> subscriptionForDeviceId = subscriptionRepository.findById(subscription.getId());
    if (subscriptionForDeviceId.isPresent())
    {
      BuildReleaseComplete buildReleaseComplete = loadBuildReleaseComplete();
      DeviceEvent deviceEventFromService = converter.convert(buildReleaseComplete);
      assertNotNull(deviceEventFromService);
      assertEquals(buildReleaseComplete.getDeviceId(), deviceEventFromService.getDeviceId());
      assertEquals(buildReleaseComplete.getStatus(), deviceEventFromService.getActivityState());
      assertEquals(buildReleaseComplete.getSource().getLocationId(),
                   deviceEventFromService.getLocation().getLocationId());
    }
    else
    {
      fail("No Device Event subscription");
    }
  }

  @Test
  void deviceEventValidatorTest() throws IOException
  {
    BuildReleaseComplete buildReleaseComplete = loadBuildReleaseComplete();
    assertNotNull(buildReleaseComplete);
    Location source = Location.builder()
        .locationId(buildReleaseComplete.getSource().getLocationId())
        .build();
    DeviceEvent deviceEvent = DeviceEvent.builder()
        .systemId(properties.getSystemId())
        .deviceId(buildReleaseComplete.getDeviceId())
        .activityState(buildReleaseComplete.getStatus())
        .activityId(buildReleaseComplete.getBuildId())
        .location(source)
        .timestamp(OffsetDateTime.now())
        .build();
    validator.validate(deviceEvent);
  }

  public BuildReleaseComplete loadBuildReleaseComplete() throws IOException
  {
    InputStream inputStream = TypeReference.class.getResourceAsStream
        ("/sample-messages/from-palletizer/build-release-complete.json");
    return objectMapper.readValue(inputStream, getReference());
  }

  protected TypeReference<BuildReleaseComplete> getReference()
  {
    return new TypeReference<>()
    {
    };
  }
}
