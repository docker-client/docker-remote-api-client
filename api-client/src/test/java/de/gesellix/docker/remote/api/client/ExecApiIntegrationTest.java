package de.gesellix.docker.remote.api.client;

import de.gesellix.docker.remote.api.ContainerCreateRequest;
import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.ExecConfig;
import de.gesellix.docker.remote.api.ExecInspectResponse;
import de.gesellix.docker.remote.api.ExecStartConfig;
import de.gesellix.docker.remote.api.IdResponse;
import de.gesellix.docker.remote.api.testutil.DockerEngineAvailable;
import de.gesellix.docker.remote.api.testutil.InjectDockerClient;
import de.gesellix.docker.remote.api.testutil.TestImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_KEY;
import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_VALUE;
import static de.gesellix.docker.remote.api.testutil.Failsafe.removeContainer;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
