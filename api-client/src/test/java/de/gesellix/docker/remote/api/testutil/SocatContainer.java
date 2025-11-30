package de.gesellix.docker.remote.api.testutil;

import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_KEY;
import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_VALUE;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import de.gesellix.docker.engine.DockerClientConfig;
import de.gesellix.docker.remote.api.ContainerCreateRequest;
import de.gesellix.docker.remote.api.ContainerCreateResponse;
import de.gesellix.docker.remote.api.ContainerInspectResponse;
import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.EngineApiClientImpl;
import de.gesellix.docker.remote.api.HostConfig;

public class SocatContainer {

  private final EngineApiClient engineApiClient;
  private final String repository;
  private final String tag;

  public SocatContainer(EngineApiClient engineApiClient) {
    this.engineApiClient = engineApiClient;
    this.repository = "gesellix/socat";
    this.tag = "os-linux";
  }

  public EngineApiClient getEngineApiClient() {
    return engineApiClient;
  }

  public void stopSocatContainer() {
    engineApiClient.getContainerApi().containerStop("socat", null);
  }

  public EngineApiClient startSocatContainer() {
    if (!EnabledIfSupportsWebSocket.WebSocketCondition.isUnixSocket()) {
      return engineApiClient;
    }
    // use a socat "tcp proxy" to test the websocket communication
    engineApiClient.getImageApi().imageCreate(getImageName(), null, null, getImageTag(), null, null, null, null, null);
    HostConfig hostConfig = new HostConfig();
    hostConfig.setAutoRemove(true);
    hostConfig.setPublishAllPorts(true);
    hostConfig.setBinds(singletonList("/var/run/docker.sock:/var/run/docker.sock"));
    ContainerCreateRequest socatContainerConfig = new ContainerCreateRequest();
    socatContainerConfig.setAttachStdin(true);
    socatContainerConfig.setAttachStdout(true);
    socatContainerConfig.setAttachStderr(true);
    socatContainerConfig.setTty(true);
    socatContainerConfig.setOpenStdin(true);
    socatContainerConfig.setImage(getImageWithTag());
    socatContainerConfig.setLabels(singletonMap(LABEL_KEY, LABEL_VALUE));
    socatContainerConfig.setHostConfig(hostConfig);
    ContainerCreateResponse socatContainer = engineApiClient.getContainerApi().containerCreate(socatContainerConfig, "socat");
    engineApiClient.getContainerApi().containerStart("socat", null);
    String socatId = socatContainer.getId();
    ContainerInspectResponse socatDetails = engineApiClient.getContainerApi().containerInspect(socatId, false);
    String socatContainerPort = socatDetails.getNetworkSettings().getPorts().get("2375/tcp").get(0).getHostPort();
    EngineApiClientImpl tcpClient = new EngineApiClientImpl(new DockerClientConfig("tcp://localhost:" + socatContainerPort));
    if (!tcpClient.getSystemApi().systemPing().equals("OK")) {
      engineApiClient.getContainerApi().containerStop("socat", null);
      throw new IllegalStateException("ping failed via socat");
    }
    return tcpClient;
  }

  public String getImageWithTag() {
    return getImageName() + ":" + getImageTag();
  }

  public String getImageName() {
    return repository;
  }

  public String getImageTag() {
    return tag;
  }
}
