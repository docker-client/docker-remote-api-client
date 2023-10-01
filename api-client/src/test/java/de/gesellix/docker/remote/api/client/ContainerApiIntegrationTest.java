package de.gesellix.docker.remote.api.client;

import com.squareup.moshi.Moshi;
import de.gesellix.docker.remote.api.ContainerCreateRequest;
import de.gesellix.docker.remote.api.ContainerCreateResponse;
import de.gesellix.docker.remote.api.ContainerInspectResponse;
import de.gesellix.docker.remote.api.ContainerPruneResponse;
import de.gesellix.docker.remote.api.ContainerSummary;
import de.gesellix.docker.remote.api.ContainerTopResponse;
import de.gesellix.docker.remote.api.ContainerUpdateRequest;
import de.gesellix.docker.remote.api.ContainerUpdateResponse;
import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.ExecConfig;
import de.gesellix.docker.remote.api.ExecStartConfig;
import de.gesellix.docker.remote.api.IdResponse;
import de.gesellix.docker.remote.api.RestartPolicy;
import de.gesellix.docker.remote.api.core.Cancellable;
import de.gesellix.docker.remote.api.core.ClientException;
import de.gesellix.docker.remote.api.core.Frame;
import de.gesellix.docker.remote.api.core.LoggingExtensionsKt;
import de.gesellix.docker.remote.api.core.StreamCallback;
import de.gesellix.docker.remote.api.testutil.DisabledIfDaemonOnWindowsOs;
import de.gesellix.docker.remote.api.testutil.DisabledIfNotPausable;
import de.gesellix.docker.remote.api.testutil.DockerEngineAvailable;
import de.gesellix.docker.remote.api.testutil.Failsafe;
import de.gesellix.docker.remote.api.testutil.InjectDockerClient;
import de.gesellix.docker.remote.api.testutil.TarUtil;
import de.gesellix.docker.remote.api.testutil.TestImage;
import okio.Okio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_KEY;
import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_VALUE;
import static de.gesellix.docker.remote.api.testutil.Failsafe.removeContainer;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DockerEngineAvailable
class ContainerApiIntegrationTest {

  private static final Logger log = LoggingExtensionsKt.logger(ContainerApiIntegrationTest.class.getName()).getValue();

  @InjectDockerClient
  private EngineApiClient engineApiClient;

  private TestImage testImage;

  ContainerApi containerApi;
  ImageApi imageApi;

  @BeforeEach
  public void setup() {
    containerApi = engineApiClient.getContainerApi();
    imageApi = engineApiClient.getImageApi();
    testImage = new TestImage(engineApiClient);
  }

  @Test
  public void containerList() {
    List<ContainerSummary> containers = containerApi.containerList(null, null, null, null);
    assertNotNull(containers);
  }

  @Test
  public void containerCreate() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        singletonList("-"),
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    ContainerCreateResponse container = containerApi.containerCreate(containerCreateRequest, "container-create-test");
    assertTrue(container.getId().matches("\\w+"));

    removeContainer(engineApiClient, "container-create-test");
  }

  @Test
  public void containerDelete() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        singletonList("-"),
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-delete-test");
    assertDoesNotThrow(() -> containerApi.containerDelete("container-delete-test", null, null, null));
    assertDoesNotThrow(() -> containerApi.containerDelete("container-delete-missing", null, null, null));

    Failsafe.perform(() -> imageApi.imageDelete(testImage.getImageWithTag(), null, null));
  }

  // WCOW does not support exporting containers
  // See https://github.com/moby/moby/issues/33581
  @DisabledIfDaemonOnWindowsOs
  @Test
  public void containerExport() {
    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-export");
    Long size = assertDoesNotThrow(() -> {
      InputStream inputStream = containerApi.containerExport("container-export");
      long byteCount = Okio.buffer(Okio.source(inputStream)).readAll(Okio.blackhole());
      inputStream.close();
      return byteCount;
    });
    assertTrue(size > 0);
    removeContainer(engineApiClient, "container-export");
  }

  @Test
  public void containerArchiveInfoGetAndPut() throws IOException {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        singletonList("-"),
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-archive-info-test");

    // filesystem operations against a running Hyper-V container are not supported,
    // so we prepare the container before starting it
    InputStream archive = containerApi.containerArchive("container-archive-info-test", "/gattaca.txt");
    File extractedDir = new TarUtil().unTar(archive);
    File gattaca = new File(extractedDir, "gattaca.txt");
    String fileContent = Okio.buffer(Okio.source(gattaca)).readUtf8();
    assertEquals("The wind\ncaught it.\n", fileContent.replaceAll("\r", ""));

    String testPath = testImage.isWindowsContainer()
                      ? "tmp\\test"
                      : "/tmp/test/";
    List<String> execCmd = testImage.isWindowsContainer()
                           ? Arrays.asList("cmd", "/C", "mkdir " + testPath)
                           : Arrays.asList("mkdir", testPath);

    containerApi.containerStart("container-archive-info-test", null);
    IdResponse containerExec = engineApiClient.getExecApi().containerExec(
        "container-archive-info-test",
        new ExecConfig(null, null, null, null, null, null, null,
                       execCmd,
                       null, null, null));
    engineApiClient.getExecApi().execStart(
        containerExec.getId(),
        new ExecStartConfig(null, null, null));

    // filesystem operations against a running Hyper-V container are not supported,
    // so we stop the container before using the archive container api
    containerApi.containerStop("container-archive-info-test", null);
    containerApi.containerWait("container-archive-info-test", ContainerApi.ConditionContainerWait.NotMinusRunning);

    containerApi.putContainerArchive("container-archive-info-test", testPath, new TarUtil().tar(gattaca), null, null);

    Map<String, Object> archiveCopyInfo = (Map<String, Object>) containerApi.containerArchiveInfo("container-archive-info-test", testPath + "/gattaca.txt");
    assertEquals("gattaca.txt", archiveCopyInfo == null ? null : archiveCopyInfo.get("name"));

    removeContainer(engineApiClient, "container-archive-info-test");
  }

  @Test
  public void containerInspect() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        singletonList("-"),
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-inspect-test");
    ContainerInspectResponse container = containerApi.containerInspect("container-inspect-test", false);
    assertEquals("/container-inspect-test", container.getName());

    removeContainer(engineApiClient, "container-inspect-test");
  }

  @Test
  public void containerInspectMissing() {
    ClientException clientException = assertThrows(ClientException.class, () -> containerApi.containerInspect("random-" + UUID.randomUUID(), false));
    assertEquals(404, clientException.getStatusCode());
  }

  @Test
  public void containerRename() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        singletonList("-"),
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-rename-test");
    assertDoesNotThrow(() -> containerApi.containerRename("container-rename-test", "fancy-name"));

    removeContainer(engineApiClient, "fancy-name");
  }

  @Test
  public void containerStartStopWait() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-start-test");
    containerApi.containerStart("container-start-test", null);
    ContainerInspectResponse container = containerApi.containerInspect("container-start-test", false);
    assertTrue(container.getState().getRunning());

    removeContainer(engineApiClient, "container-start-test");
    assertDoesNotThrow(() -> containerApi.containerStop("container-start-test", null));
  }

  @Test
  public void containerLogsWithoutTty() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-logs-test");
    containerApi.containerStart("container-logs-test", null);

    Duration timeout = Duration.of(10, SECONDS);
    LogFrameStreamCallback callback = new LogFrameStreamCallback();

    new Thread(() -> containerApi.containerLogs(
        "container-logs-test",
        false, true, true, null, null, null, null,
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

    removeContainer(engineApiClient, "container-logs-test");

    Optional<Frame> frame = callback.frames.stream().findAny();

    // TODO we should improve the test or even the production code to be more reliable
//    assertTrue(frame.isPresent());
    assumeTrue(frame.isPresent());

    assertSame(frame.get().getStreamType(), Frame.StreamType.STDOUT);
  }

  @Test
  public void containerLogsWithTty() throws InterruptedException {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        true, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-logs-with-tty-test");
    containerApi.containerStart("container-logs-with-tty-test", null);
    Thread.sleep(500);

    Duration timeout = Duration.of(10, SECONDS);
    LogFrameStreamCallback callback = new LogFrameStreamCallback();

    new Thread(() -> containerApi.containerLogs(
        "container-logs-with-tty-test",
        false, true, true, null, null, null, null,
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
    Optional<Frame> anyFrame = callback.frames.stream().findAny();
    assertTrue(anyFrame.isPresent());
    assertSame(anyFrame.get().getStreamType(), Frame.StreamType.RAW);

    removeContainer(engineApiClient, "container-logs-with-tty-test");
  }

  @Test
  public void containerUpdate() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-update-test");
    containerApi.containerStart("container-update-test", null);
    ContainerUpdateRequest updateRequest = new ContainerUpdateRequest(
        null, null, null,
        null, null, null, null, null, null,
        null, null, null, null, null, null,
        null, null, null,
        null, null, null, null,
        null, null, null, null, null,
        null, null, null, null,
        new RestartPolicy(RestartPolicy.Name.UnlessMinusStopped, null));
    ContainerUpdateResponse updateResponse = containerApi.containerUpdate("container-update-test", updateRequest);
    assertTrue(updateResponse.getWarnings() == null || updateResponse.getWarnings().isEmpty());

    removeContainer(engineApiClient, "container-update-test");
  }

  @Test
  public void containerRestart() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-restart-test");
    containerApi.containerStart("container-restart-test", null);
    assertDoesNotThrow(() -> containerApi.containerRestart("container-restart-test", 5));

    removeContainer(engineApiClient, "container-restart-test");
  }

  @Test
  public void containerKill() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-kill-test");
    containerApi.containerStart("container-kill-test", null);
    assertDoesNotThrow(() -> containerApi.containerKill("container-kill-test", null));

    removeContainer(engineApiClient, "container-kill-test");
  }

  @DisabledIfNotPausable
  @Test
  public void containerPauseUnpause() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-pause-test");
    containerApi.containerStart("container-pause-test", null);
    assertDoesNotThrow(() -> containerApi.containerPause("container-pause-test"));
    ContainerInspectResponse pausedContainer = containerApi.containerInspect("container-pause-test", false);
    assertTrue(pausedContainer.getState().getPaused());
    assertDoesNotThrow(() -> containerApi.containerUnpause("container-pause-test"));
    ContainerInspectResponse unpausedContainer = containerApi.containerInspect("container-pause-test", false);
    assertFalse(unpausedContainer.getState().getPaused());

    removeContainer(engineApiClient, "container-pause-test");
  }

  @Test
  public void containerPrune() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-prune-test");

    Map<String, List<String>> filter = new HashMap<>();
    filter.put("label", singletonList(LABEL_KEY));
    String filterJson = new Moshi.Builder().build().adapter(Map.class).toJson(filter);

    Optional<ContainerSummary> toBePruned = containerApi.containerList(true, null, null, filterJson).stream().filter((c) -> c.getNames().contains("/container-prune-test")).findFirst();
    assertTrue(toBePruned.isPresent());

    ContainerPruneResponse pruneResponse = containerApi.containerPrune(filterJson);
    assertTrue(pruneResponse.getContainersDeleted().contains(toBePruned.get().getId()));

    Optional<ContainerSummary> shouldBeMissing = containerApi.containerList(true, null, null, filterJson).stream().filter((c) -> c.getNames().contains("/container-prune-test")).findFirst();
    assertFalse(shouldBeMissing.isPresent());

    removeContainer(engineApiClient, "container-prune-test");
  }

  // the api reference v1.41 says: "On Unix systems, this is done by running the ps command. This endpoint is not supported on Windows."
  @Test
  public void containerTop() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-top-test");
    containerApi.containerStart("container-top-test", null);

    ContainerTopResponse processes = containerApi.containerTop("container-top-test", null);
    final String processTitle;
    if (processes.getTitles().contains("CMD")) {
      // Linux, macOS
      processTitle = "CMD";
    } else if (processes.getTitles().contains("COMMAND")) {
      // macOS
      processTitle = "COMMAND";
    } else {
      // Windows
      processTitle = "Name";
    }
    List<List<String>> mainProcesses = processes.getProcesses()
        .stream()
        .filter((p) -> p.get(processes.getTitles().indexOf(processTitle)).contains("main"))
        .collect(Collectors.toList());

    assertEquals(1, mainProcesses.size());

    removeContainer(engineApiClient, "container-top-test");
  }

  @Test
  public void containerStatsStream() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-stats-test");
    containerApi.containerStart("container-stats-test", null);

    Duration timeout = Duration.of(5, SECONDS);
    LogObjectStreamCallback callback = new LogObjectStreamCallback();

    containerApi.containerStats("container-stats-test", null, null, callback, timeout.toMillis());
    assertFalse(callback.elements.isEmpty());

    removeContainer(engineApiClient, "container-stats-test");
  }

  @Test
  public void containerStatsOnce() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-stats-test");
    containerApi.containerStart("container-stats-test", null);

    Duration timeout = Duration.of(5, SECONDS);
    LogObjectStreamCallback callback = new LogObjectStreamCallback();

    containerApi.containerStats("container-stats-test", false, null, callback, timeout.toMillis());
    assertFalse(callback.elements.isEmpty());

    removeContainer(engineApiClient, "container-stats-test");
  }

  @Test
  public void containerAttachNonInteractive() {
    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        null,
        null,
        null,
        testImage.getImageWithTag(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    containerApi.containerCreate(containerCreateRequest, "container-attach-non-interactive-test");
    containerApi.containerStart("container-attach-non-interactive-test", null);

    Duration timeout = Duration.of(5, SECONDS);
    LogFrameStreamCallback callback = new LogFrameStreamCallback();

    new Thread(() -> containerApi.containerAttach(
        "container-attach-non-interactive-test",
        null, true, true, null, true, true,
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
    assertSame(callback.frames.stream().findAny().get().getStreamType(), Frame.StreamType.STDOUT);

    removeContainer(engineApiClient, "container-attach-non-interactive-test");
  }

  static class LogFrameStreamCallback implements StreamCallback<Frame> {

    List<Frame> frames = new ArrayList<>();
    Cancellable job = null;

    @Override
    public void onStarting(Cancellable cancellable) {
      job = cancellable;
    }

    @Override
    public void onNext(Frame frame) {
      frames.add(frame);
      log.info("next: {}", frame);
    }
  }

  static class LogObjectStreamCallback implements StreamCallback<Object> {

    List<Object> elements = new ArrayList<>();
    Cancellable job = null;

    @Override
    public void onStarting(Cancellable cancellable) {
      job = cancellable;
    }

    @Override
    public void onNext(Object element) {
      elements.add(element);
      log.info("next: {}", element);
    }
  }
}
