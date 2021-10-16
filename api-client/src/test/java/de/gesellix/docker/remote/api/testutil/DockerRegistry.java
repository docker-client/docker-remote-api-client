package de.gesellix.docker.remote.api.testutil;

import de.gesellix.docker.remote.api.ContainerCreateRequest;
import de.gesellix.docker.remote.api.ContainerCreateResponse;
import de.gesellix.docker.remote.api.ContainerInspectResponse;
import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.EngineApiClientImpl;
import de.gesellix.docker.remote.api.HostConfig;
import de.gesellix.docker.remote.api.PortBinding;

import java.util.List;
import java.util.Objects;

import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_KEY;
import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_VALUE;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

public class DockerRegistry {

  EngineApiClient engineApiClient;
  String registryId;

  public static void main(String[] args) throws InterruptedException {
    DockerRegistry registry = new DockerRegistry(new EngineApiClientImpl());
    registry.run();
    Thread.sleep(10000);
    registry.rm();
  }

  public DockerRegistry(EngineApiClient engineApiClient) {
    this.engineApiClient = engineApiClient;
  }

  String getImage() {
//    dockerClient.getSystemApi().systemInfo().getOsType()
    boolean isWindows = Objects.requireNonNull(engineApiClient.getSystemApi().systemVersion().getOs()).equalsIgnoreCase("windows");
    return isWindows ? "gesellix/registry:2.7.1-windows" : "registry:2.7.1";
  }

  public void run() {
    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        singletonMap("5000/tcp", emptyMap()),
        false, null, null,
        null,
        null,
        null,
        null,
        getImage(),
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        new HostConfig(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                       null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                       null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                       null, null, true, null, null, null, null, null, null, null, null, null, null, null, null, null),
        null
    );
    engineApiClient.getImageApi().imageCreate(containerCreateRequest.getImage().split(":")[0], null, null, containerCreateRequest.getImage().split(":")[1], null, null, null, null, null);
    ContainerCreateResponse createResponse = engineApiClient.getContainerApi().containerCreate(containerCreateRequest, null);
    engineApiClient.getContainerApi().containerStart(createResponse.getId(), null);
    registryId = createResponse.getId();
  }

  String address() {
//        String dockerHost = dockerClient.config.dockerHost
//        return dockerHost.replaceAll("^(tcp|http|https)://", "").replaceAll(":\\d+\$", "")

//        def registryContainer = dockerClient.inspectContainer(registryId).content
//        def portBinding = registryContainer.NetworkSettings.Ports["5000/tcp"]
//        return portBinding[0].HostIp as String

    // 'localhost' allows to use the registry without TLS
    return "localhost";
  }

  int port() {
    ContainerInspectResponse registryContainer = engineApiClient.getContainerApi().containerInspect(registryId, false);
    List<PortBinding> portBinding = registryContainer.getNetworkSettings().getPorts().get("5000/tcp");
    return Integer.parseInt(portBinding.stream().findFirst().get().getHostPort());
  }

  public String url() {
    return address() + ":" + port();
  }

  public void rm() {
    engineApiClient.getContainerApi().containerStop(registryId, null);
    engineApiClient.getContainerApi().containerWait(registryId, null);
    engineApiClient.getContainerApi().containerDelete(registryId, null, null, null);
  }
}
