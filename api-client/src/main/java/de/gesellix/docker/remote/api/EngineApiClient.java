package de.gesellix.docker.remote.api;

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

public interface EngineApiClient {

  ConfigApi getConfigApi();

  ContainerApi getContainerApi();

  DistributionApi getDistributionApi();

  ExecApi getExecApi();

  ImageApi getImageApi();

  NetworkApi getNetworkApi();

  NodeApi getNodeApi();

  PluginApi getPluginApi();

  SecretApi getSecretApi();

  ServiceApi getServiceApi();

  SwarmApi getSwarmApi();

  SystemApi getSystemApi();

  TaskApi getTaskApi();

  VolumeApi getVolumeApi();
}
