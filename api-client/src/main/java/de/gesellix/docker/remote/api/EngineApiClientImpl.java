package de.gesellix.docker.remote.api;

import de.gesellix.docker.engine.DockerClientConfig;
import de.gesellix.docker.remote.api.client.ConfigApi;
import de.gesellix.docker.remote.api.client.ContainerApi;
import de.gesellix.docker.remote.api.client.DistributionApi;
import de.gesellix.docker.remote.api.client.ExecApi;
import de.gesellix.docker.remote.api.client.ImageApi;
import de.gesellix.docker.remote.api.client.NetworkApi;
import de.gesellix.docker.remote.api.client.NodeApi;
import de.gesellix.docker.remote.api.client.PluginApi;
import de.gesellix.docker.remote.api.client.SecretApi;
import de.gesellix.docker.remote.api.client.ServiceApi;
import de.gesellix.docker.remote.api.client.SwarmApi;
import de.gesellix.docker.remote.api.client.SystemApi;
import de.gesellix.docker.remote.api.client.TaskApi;
import de.gesellix.docker.remote.api.client.VolumeApi;

import java.net.Proxy;

public class EngineApiClientImpl implements EngineApiClient {

  private final ConfigApi configApi;
  private final ContainerApi containerApi;
  private final DistributionApi distributionApi;
  private final ExecApi execApi;
  private final ImageApi imageApi;
  private final NetworkApi networkApi;
  private final NodeApi nodeApi;
  private final PluginApi pluginApi;
  private final SecretApi secretApi;
  private final ServiceApi serviceApi;
  private final SwarmApi swarmApi;
  private final SystemApi systemApi;
  private final TaskApi taskApi;
  private final VolumeApi volumeApi;

  public EngineApiClientImpl() {
    this(new DockerClientConfig());
  }

  public EngineApiClientImpl(DockerClientConfig dockerClientConfig) {
    this(dockerClientConfig, null);
//    this(dockerClientConfig, Proxy.NO_PROXY);
  }

  public EngineApiClientImpl(DockerClientConfig dockerClientConfig, Proxy proxy) {
    configApi = new ConfigApi(dockerClientConfig, proxy);
    containerApi = new ContainerApi(dockerClientConfig, proxy);
    distributionApi = new DistributionApi(dockerClientConfig, proxy);
    execApi = new ExecApi(dockerClientConfig, proxy);
    imageApi = new ImageApi(dockerClientConfig, proxy);
    networkApi = new NetworkApi(dockerClientConfig, proxy);
    nodeApi = new NodeApi(dockerClientConfig, proxy);
    pluginApi = new PluginApi(dockerClientConfig, proxy);
    secretApi = new SecretApi(dockerClientConfig, proxy);
    serviceApi = new ServiceApi(dockerClientConfig, proxy);
    swarmApi = new SwarmApi(dockerClientConfig, proxy);
    systemApi = new SystemApi(dockerClientConfig, proxy);
    taskApi = new TaskApi(dockerClientConfig, proxy);
    volumeApi = new VolumeApi(dockerClientConfig, proxy);
  }

  @Override
  public ConfigApi getConfigApi() {
    return configApi;
  }

  @Override
  public ContainerApi getContainerApi() {
    return containerApi;
  }

  @Override
  public DistributionApi getDistributionApi() {
    return distributionApi;
  }

  @Override
  public ExecApi getExecApi() {
    return execApi;
  }

  @Override
  public ImageApi getImageApi() {
    return imageApi;
  }

  @Override
  public NetworkApi getNetworkApi() {
    return networkApi;
  }

  @Override
  public NodeApi getNodeApi() {
    return nodeApi;
  }

  @Override
  public PluginApi getPluginApi() {
    return pluginApi;
  }

  @Override
  public SecretApi getSecretApi() {
    return secretApi;
  }

  @Override
  public ServiceApi getServiceApi() {
    return serviceApi;
  }

  @Override
  public SwarmApi getSwarmApi() {
    return swarmApi;
  }

  @Override
  public SystemApi getSystemApi() {
    return systemApi;
  }

  @Override
  public TaskApi getTaskApi() {
    return taskApi;
  }

  @Override
  public VolumeApi getVolumeApi() {
    return volumeApi;
  }
}
