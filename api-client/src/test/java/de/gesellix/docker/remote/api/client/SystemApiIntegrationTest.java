package de.gesellix.docker.remote.api.client;

import de.gesellix.docker.authentication.AuthConfigReader;
import de.gesellix.docker.remote.api.AuthConfig;
import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.EventMessage;
import de.gesellix.docker.remote.api.SystemAuthResponse;
import de.gesellix.docker.remote.api.SystemInfo;
import de.gesellix.docker.remote.api.SystemVersion;
import de.gesellix.docker.remote.api.core.Cancellable;
import de.gesellix.docker.remote.api.core.ClientException;
import de.gesellix.docker.remote.api.core.LoggingExtensionsKt;
import de.gesellix.docker.remote.api.core.StreamCallback;
import de.gesellix.docker.remote.api.testutil.DockerEngineAvailable;
import de.gesellix.docker.remote.api.testutil.InjectDockerClient;
import de.gesellix.docker.remote.api.testutil.TestImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DockerEngineAvailable
class SystemApiIntegrationTest {

  private static final Logger log = LoggingExtensionsKt.logger(SystemApiIntegrationTest.class.getName()).getValue();

  @InjectDockerClient
  private EngineApiClient engineApiClient;

  private TestImage testImage;

  SystemApi systemApi;
  ImageApi imageApi;
  ContainerApi containerApi;

  @BeforeEach
  public void setup() {
    systemApi = engineApiClient.getSystemApi();
    imageApi = engineApiClient.getImageApi();
    containerApi = engineApiClient.getContainerApi();
    testImage = new TestImage(engineApiClient);
  }

  @Test
  public void systemAuthWhenUnauthorized() {
    assertThrows(ClientException.class, () -> systemApi.systemAuth(new AuthConfig("unknown-username", "a-secret", "user@example.com", null)));
  }

  @Test
  public void systemAuthWhenAuthorized() {
    de.gesellix.docker.authentication.AuthConfig defaultAuthConfig = new AuthConfigReader().readDefaultAuthConfig();
    SystemAuthResponse authResponse = systemApi.systemAuth(new AuthConfig(defaultAuthConfig.getUsername(), defaultAuthConfig.getPassword(), null, null));
    assertEquals("Login Succeeded", authResponse.getStatus());
  }

  @Test
  public void systemDataUsage() {
    assertDoesNotThrow(() -> systemApi.systemDataUsage());
  }

  @Test
  public void systemEvents() {
    Duration timeout = Duration.of(20, SECONDS);
    Instant since = ZonedDateTime.now().toInstant();
    Instant until = ZonedDateTime.now().plus(timeout).plusSeconds(10).toInstant();
    SystemEventsCallback callback = new SystemEventsCallback();

    new Thread(() -> systemApi.systemEvents(
        "" + since.getEpochSecond(),
        "" + until.getEpochSecond(),
        null,
        callback,
        timeout.toMillis())).start();

    try {
      Thread.sleep(10);
    }
    catch (InterruptedException e) {
      log.warn("ignoring interrupted wait", e);
    }

    imageApi.imageTag(testImage.getImageWithTag(), "test", "system-events");
    imageApi.imageDelete("test:system-events", null, null);

    CountDownLatch wait = new CountDownLatch(1);
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        callback.job.cancel();
        wait.countDown();
      }
    }, 1000);
    try {
      wait.await();
    }
    catch (InterruptedException e) {
      log.warn("interrupted", e);
    }

    EventMessage event = callback.events.stream().filter(e -> Objects.equals(e.getAction(), "tag")).findFirst().orElse(new EventMessage());
    assertEquals(EventMessage.Type.Image, event.getType());
  }

  @Test
  public void systemInfo() {
    SystemInfo systemInfo = systemApi.systemInfo();
    assertEquals("docker.io", systemInfo.getRegistryConfig().getIndexConfigs().get("docker.io").getName());
    assertTrue(systemInfo.getRegistryConfig().getIndexConfigs().get("docker.io").getOfficial());
    assertTrue(systemInfo.getRegistryConfig().getIndexConfigs().get("docker.io").getSecure());
    assertTrue(null == systemInfo.getIsolation() || asList(SystemInfo.Isolation.values()).contains(systemInfo.getIsolation()));
  }

  @Test
  public void systemPing() {
    String systemPing = systemApi.systemPing();
    assertEquals("OK", systemPing);
  }

  @Test
  public void systemPingHead() {
    String systemPing = systemApi.systemPingHead();
    assertEquals("", systemPing);
  }

  @Test
  public void systemVersion() {
    SystemVersion systemVersion = systemApi.systemVersion();
    assertTrue(asList("1.42", "1.43", "1.44", "1.45", "1.46", "1.47", "1.48").contains(systemVersion.getApiVersion()));
  }

  static class SystemEventsCallback implements StreamCallback<EventMessage> {

    List<EventMessage> events = new ArrayList<>();
    Cancellable job = null;

    @Override
    public void onStarting(Cancellable cancellable) {
      job = cancellable;
    }

    @Override
    public void onNext(EventMessage event) {
      events.add(event);
      log.info("{}", event);
    }
  }
}
