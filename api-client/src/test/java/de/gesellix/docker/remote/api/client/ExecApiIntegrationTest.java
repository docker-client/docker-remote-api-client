package de.gesellix.docker.remote.api.client;

import de.gesellix.docker.registry.LocalDocker;
import de.gesellix.docker.remote.api.ContainerCreateRequest;
import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.ExecConfig;
import de.gesellix.docker.remote.api.ExecInspectResponse;
import de.gesellix.docker.remote.api.ExecStartConfig;
import de.gesellix.docker.remote.api.IdResponse;
import de.gesellix.docker.remote.api.core.Frame;
import de.gesellix.docker.remote.api.testutil.DockerEngineAvailable;
import de.gesellix.docker.remote.api.testutil.InjectDockerClient;
import de.gesellix.docker.remote.api.testutil.TestImage;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_KEY;
import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_VALUE;
import static de.gesellix.docker.remote.api.testutil.Failsafe.perform;
import static de.gesellix.docker.remote.api.testutil.Failsafe.removeContainer;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@DockerEngineAvailable
class ExecApiIntegrationTest {

  @InjectDockerClient
  private EngineApiClient engineApiClient;

  private TestImage testImage;

  ExecApi execApi;
  ContainerApi containerApi;
  ImageApi imageApi;

  @BeforeEach
  public void setup() {
    execApi = engineApiClient.getExecApi();
    containerApi = engineApiClient.getContainerApi();
    imageApi = engineApiClient.getImageApi();
    testImage = new TestImage(engineApiClient);
  }

  @Test
  public void containerExecNonInteractive() {
    removeContainer(engineApiClient, "container-exec-test");

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
    containerApi.containerCreate(containerCreateRequest, "container-exec-test");
    containerApi.containerStart("container-exec-test", null);

    IdResponse exec = execApi.containerExec(
        "container-exec-test",
        new ExecConfig(null, true, true, null, null, null,
                       null,
                       asList("echo", "'aus dem Wald'"),
                       null, null, null));
    assertNotNull(exec.getId());

    execApi.execStart(exec.getId(), new ExecStartConfig(false, null, null));

    ExecInspectResponse execInspect = execApi.execInspect(exec.getId());
    assertFalse(execInspect.getRunning());

    removeContainer(engineApiClient, "container-exec-test");
  }

  @Test
  public void containerExecInteractive() {
    removeContainer(engineApiClient, "container-exec-interactive-test");

    imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null);

    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        true, true, true,
        null,
        true, true, null,
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
    containerApi.containerCreate(containerCreateRequest, "container-exec-interactive-test");
    containerApi.containerStart("container-exec-interactive-test", null);

    ExecConfig execConfig = new ExecConfig(true, true, true, null, null, false,
        null,
        singletonList("/cat"),
        null, null, null);

    if (LocalDocker.isNativeWindows()) {
      execConfig.setTty(false);
      execConfig.setCmd(Arrays.asList("cmd", "/V:ON", "/C", "set /p line= & echo #!line!#"));
    } else {
      execConfig.setTty(false);
      execConfig.setCmd(Arrays.asList("/bin/sh", "-c", "read line && echo \"#$line#\""));
    }

    IdResponse exec = execApi.containerExec("container-exec-interactive-test", execConfig);
    assertNotNull(exec.getId());

    Duration timeout = Duration.of(5, SECONDS);
    LogFrameStreamCallback callback = new LogFrameStreamCallback() {
      @Override
      public void attachInput(Sink sink) {
        System.out.println("attachInput, sending data...");
        new Thread(() -> {
          BufferedSink buffer = Okio.buffer(sink);
          try {
            buffer.writeUtf8("hello echo\n");
            buffer.flush();
            System.out.println("... data sent");
          } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to write to stdin: " + e.getMessage());
          } finally {
            try {
              Thread.sleep(100);
              sink.close();
            } catch (Exception ignored) {
              // ignore
            }
          }
        }).start();
      }
    };

    new Thread(() -> execApi.execStart(
        exec.getId(),
        new ExecStartConfig(false, true, null),
        callback, timeout.toMillis())).start();

    CountDownLatch wait = new CountDownLatch(1);
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        if (callback.job != null) {
          callback.job.cancel();
        }
        wait.countDown();
      }
    }, 5000);

    try {
      wait.await();
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }

//    ExecInspectResponse execInspect = execApi.execInspect(exec.getId());
//    assertTrue(execInspect.getRunning());

    if (execConfig.getTty() != null && execConfig.getTty()) {
      assertSame(Frame.StreamType.RAW, callback.frames.stream().findAny().get().getStreamType());
      assertEquals(
          "hello echo\nhello echo".replaceAll("[\\n\\r]", ""),
          callback.frames.stream().map(Frame::getPayloadAsString).collect(Collectors.joining()).replaceAll("[\\n\\r]", ""));
    } else {
      assertSame(Frame.StreamType.RAW, callback.frames.stream().findAny().get().getStreamType());
      assertEquals(
          "#hello echo#",
          callback.frames.stream().map(Frame::getPayloadAsString).collect(Collectors.joining()).replaceAll("[\\n\\r]", ""));
    }

    removeContainer(engineApiClient, "container-exec-interactive-test");

    perform(() -> {
      ExecInspectResponse execInspectAfterStop = execApi.execInspect(exec.getId());
      assertFalse(execInspectAfterStop.getRunning());
    });
  }
}
