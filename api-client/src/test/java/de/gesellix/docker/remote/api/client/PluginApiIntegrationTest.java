package de.gesellix.docker.remote.api.client;

import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.Plugin;
import de.gesellix.docker.remote.api.PluginPrivilege;
import de.gesellix.docker.remote.api.testutil.DisabledIfDaemonOnWindowsOs;
import de.gesellix.docker.remote.api.testutil.DockerEngineAvailable;
import de.gesellix.docker.remote.api.testutil.InjectDockerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DockerEngineAvailable
class PluginApiIntegrationTest {

  @InjectDockerClient
  private EngineApiClient engineApiClient;

  PluginApi pluginApi;

  @BeforeEach
  public void setup() {
    pluginApi = engineApiClient.getPluginApi();
  }

  @DisabledIfDaemonOnWindowsOs
  @Test
  public void pluginList() {
    List<Plugin> plugins = pluginApi.pluginList(null);
    assertNotNull(plugins);
  }

  @DisabledIfDaemonOnWindowsOs
  @Test
  public void pluginPrivileges() {
    List<PluginPrivilege> privileges = pluginApi.getPluginPrivileges("vieux/sshfs");
    assertEquals("host", privileges.stream().filter((p) -> Objects.equals(p.getName(), "network")).findFirst().get().getValue().get(0));
  }
}
