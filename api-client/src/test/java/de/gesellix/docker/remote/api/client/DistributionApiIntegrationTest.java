package de.gesellix.docker.remote.api.client;

import de.gesellix.docker.remote.api.DistributionInspect;
import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.testutil.DockerEngineAvailable;
import de.gesellix.docker.remote.api.testutil.InjectDockerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DockerEngineAvailable
class DistributionApiIntegrationTest {

  @InjectDockerClient
  private EngineApiClient engineApiClient;

  DistributionApi distributionApi;

  @BeforeEach
  public void setup() {
    distributionApi = engineApiClient.getDistributionApi();
  }

  @Test
  public void distributionInspect() {
    DistributionInspect response = distributionApi.distributionInspect("alpine:3.5");
    assertNotNull(response.getDescriptor().getDigest());
    assertTrue(response.getDescriptor().getDigest().startsWith("sha256:"));
  }
}
