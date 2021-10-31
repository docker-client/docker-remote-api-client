package de.gesellix.docker.remote.api.client;

import com.squareup.moshi.Moshi;
import de.gesellix.docker.remote.api.EndpointPortConfig;
import de.gesellix.docker.remote.api.EndpointSpec;
import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.LocalNodeState;
import de.gesellix.docker.remote.api.ServiceCreateResponse;
import de.gesellix.docker.remote.api.ServiceServiceStatus;
import de.gesellix.docker.remote.api.ServiceSpec;
import de.gesellix.docker.remote.api.ServiceSpecMode;
import de.gesellix.docker.remote.api.ServiceSpecModeReplicated;
import de.gesellix.docker.remote.api.ServiceSpecUpdateConfig;
import de.gesellix.docker.remote.api.Task;
import de.gesellix.docker.remote.api.TaskSpec;
import de.gesellix.docker.remote.api.TaskSpecContainerSpec;
import de.gesellix.docker.remote.api.core.Cancellable;
import de.gesellix.docker.remote.api.core.Frame;
import de.gesellix.docker.remote.api.core.LoggingExtensionsKt;
import de.gesellix.docker.remote.api.core.StreamCallback;
import de.gesellix.docker.remote.api.testutil.DisabledIfDaemonOnWindowsOs;
import de.gesellix.docker.remote.api.testutil.DockerEngineAvailable;
import de.gesellix.docker.remote.api.testutil.Failsafe;
import de.gesellix.docker.remote.api.testutil.InjectDockerClient;
import de.gesellix.docker.remote.api.testutil.TestImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static de.gesellix.docker.remote.api.EndpointPortConfig.Protocol.Tcp;
import static de.gesellix.docker.remote.api.EndpointPortConfig.PublishMode.Ingress;
import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_KEY;
import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_VALUE;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

@DockerEngineAvailable(requiredSwarmMode = LocalNodeState.Active)
class TaskApiIntegrationTest {

  private static final Logger log = LoggingExtensionsKt.logger(TaskApiIntegrationTest.class.getName()).getValue();

  @InjectDockerClient
  private EngineApiClient engineApiClient;

  private TestImage testImage;

  TaskApi taskApi;
  ServiceApi serviceApi;

  @BeforeEach
  public void setup() {
    taskApi = engineApiClient.getTaskApi();
    serviceApi = engineApiClient.getServiceApi();
    testImage = new TestImage(engineApiClient);
  }

  @Test
  public void taskListInspect() throws InterruptedException {
    ServiceCreateResponse service = serviceApi.serviceCreate(
        new ServiceSpec("test-service", singletonMap(LABEL_KEY, LABEL_VALUE),
                        new TaskSpec(null, new TaskSpecContainerSpec(testImage.getImageWithTag(), singletonMap(LABEL_KEY, LABEL_VALUE),
                                                                     null, null, null, null, null,
                                                                     null, null, null,
                                                                     null, null, null, null,
                                                                     null, null, null,
                                                                     null, null, null, null,
                                                                     null, null, null, null, null, null),
                                     null, null, null, null, null, null, null, null),
                        new ServiceSpecMode(new ServiceSpecModeReplicated(1L), null, null, null),
                        new ServiceSpecUpdateConfig(1L, null, null, null, null, null),
                        null, null,
                        new EndpointSpec(null, singletonList(new EndpointPortConfig(null, Tcp, 8080, 8080, Ingress)))),
        null);

    CountDownLatch wait1 = new CountDownLatch(1);
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        Map<String, List<String>> filter = new HashMap<>();
        filter.put("name", singletonList("test-service"));
        String filterJson = new Moshi.Builder().build().adapter(Map.class).toJson(filter);
        ServiceServiceStatus serviceStatus = serviceApi.serviceList(filterJson, true).get(0).getServiceStatus();
        if (serviceStatus != null && serviceStatus.getRunningTasks() > 0) {
          wait1.countDown();
          timer.cancel();
        }
      }
    }, 500, 500);
    wait1.await(10, TimeUnit.SECONDS);
    timer.cancel();

    Map<String, List<String>> filter = new HashMap<>();
    filter.put("service", singletonList("test-service"));
    String filterJson = new Moshi.Builder().build().adapter(Map.class).toJson(filter);
    List<Task> tasks = taskApi.taskList(filterJson);
    assertFalse(tasks.isEmpty());

    Task task = taskApi.taskInspect(tasks.stream().findFirst().get().getID());
    assertEquals(service.getID(), task.getServiceID());

    serviceApi.serviceDelete("test-service");
  }

  @DisabledIfDaemonOnWindowsOs
  @Test
  public void taskLogs() throws InterruptedException {
    Failsafe.perform(() -> serviceApi.serviceDelete("test-service"));

    // TODO fails on Windows
    // "failed during hnsCallRawResponse: hnsCall failed in Win32: The parameter is incorrect. (0x57)"
    // - https://github.com/moby/moby/issues/40621
    // - https://github.com/moby/moby/issues/41094

    ServiceCreateResponse service = serviceApi.serviceCreate(
        new ServiceSpec("test-service", singletonMap(LABEL_KEY, LABEL_VALUE),
                        new TaskSpec(null, new TaskSpecContainerSpec(testImage.getImageWithTag(), singletonMap(LABEL_KEY, LABEL_VALUE),
                                                                     null, null, null, null, null,
                                                                     null, null, null,
                                                                     null, null, null, null,
                                                                     null, null, null,
                                                                     null, null, null, null,
                                                                     null, null, null, null, null, null),
                                     null, null, null, null, null, null, null, null),
                        new ServiceSpecMode(new ServiceSpecModeReplicated(1L), null, null, null),
                        new ServiceSpecUpdateConfig(1L, null, null, null, null, null),
                        null, null,
                        new EndpointSpec(null, singletonList(new EndpointPortConfig(null, Tcp, 8080, 8080, Ingress)))),
        null);

    CountDownLatch wait1 = new CountDownLatch(1);
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        Map<String, List<String>> filter = new HashMap<>();
        filter.put("name", singletonList("test-service"));
        String filterJson = new Moshi.Builder().build().adapter(Map.class).toJson(filter);
        ServiceServiceStatus serviceStatus = serviceApi.serviceList(filterJson, true).get(0).getServiceStatus();
        if (serviceStatus != null && serviceStatus.getRunningTasks() > 0) {
          wait1.countDown();
          timer.cancel();
        }
      }
    }, 500, 500);
    wait1.await(10, TimeUnit.SECONDS);
    timer.cancel();

    Map<String, List<String>> filter = new HashMap<>();
    filter.put("service", singletonList("test-service"));
    String filterJson = new Moshi.Builder().build().adapter(Map.class).toJson(filter);
    List<Task> tasks = taskApi.taskList(filterJson);
    Task task = tasks.stream().findFirst().get();

    Duration timeout = Duration.of(5, SECONDS);
    LogStreamCallback callback = new LogStreamCallback();

    new Thread(() -> taskApi.taskLogs(task.getID(),
                                      null, null, true, true, null, null, null,
                                      callback, timeout.toMillis())).start();

    CountDownLatch wait = new CountDownLatch(1);
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        callback.job.cancel();
        wait.countDown();
      }
    }, 5000);

    try {
      wait.await();
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    serviceApi.serviceDelete("test-service");
    assertSame(callback.frames.stream().findAny().get().getStreamType(), Frame.StreamType.STDOUT);
  }

  static class LogStreamCallback implements StreamCallback<Frame> {

    List<Frame> frames = new ArrayList<>();
    Cancellable job = null;

    @Override
    public void onStarting(Cancellable cancellable) {
      job = cancellable;
    }

    @Override
    public void onNext(Frame frame) {
      frames.add(frame);
      log.info("frame: {}", frame);
    }
  }
}
