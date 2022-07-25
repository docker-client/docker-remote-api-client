package de.gesellix.docker.remote.api.client;

import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.IdResponse;
import de.gesellix.docker.remote.api.LocalNodeState;
import de.gesellix.docker.remote.api.Secret;
import de.gesellix.docker.remote.api.SecretCreateRequest;
import de.gesellix.docker.remote.api.SecretSpec;
import de.gesellix.docker.remote.api.testutil.DockerEngineAvailable;
import de.gesellix.docker.remote.api.testutil.InjectDockerClient;
import de.gesellix.docker.remote.api.testutil.SwarmUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_KEY;
import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_VALUE;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DockerEngineAvailable(requiredSwarmMode = LocalNodeState.Active)
class SecretApiIntegrationTest {

  @InjectDockerClient
  private EngineApiClient engineApiClient;

  SecretApi secretApi;

  IdResponse defaultSecret;

  SwarmUtil swarmUtil;

  @BeforeEach
  public void setup() {
    secretApi = engineApiClient.getSecretApi();

    swarmUtil = new SwarmUtil(engineApiClient);

    String encoded = Base64.getEncoder().encodeToString("secret-data".getBytes());
    defaultSecret = secretApi.secretCreate(new SecretCreateRequest("secret-name", Collections.emptyMap(), encoded, null, null));
  }

  @AfterEach
  public void cleanup() {
    if (defaultSecret != null) {
      secretApi.secretDelete(defaultSecret.getId());
    }
  }

  @Test
  public void secretCreate() {
    String encoded = Base64.getEncoder().encodeToString("secret-data".getBytes());
    IdResponse response = secretApi.secretCreate(new SecretCreateRequest("my-secret", Collections.emptyMap(), encoded, null, null));
    assertTrue(response.getId().matches("\\w{5,}"));

    secretApi.secretDelete(response.getId());
  }

  @Test
  public void secretDelete() {
    String encoded = Base64.getEncoder().encodeToString("secret-data".getBytes());
    IdResponse response = secretApi.secretCreate(new SecretCreateRequest("my-secret", Collections.emptyMap(), encoded, null, null));

    assertDoesNotThrow(() -> secretApi.secretDelete(response.getId()));
  }

  @Test
  public void secretInspect() {
    Secret inspect = secretApi.secretInspect(defaultSecret.getId());
    assertEquals("secret-name", inspect.getSpec().getName());
    assertNull(inspect.getSpec().getData());
  }

  @Test
  public void secretList() {
    List<Secret> secrets = secretApi.secretList(null);
    Stream<Secret> filtered = secrets.stream().filter(c -> Objects.equals(c.getID(), defaultSecret.getId()));
    assertEquals(defaultSecret.getId(), filtered.findFirst().orElse(new Secret()).getID());
  }

  @Test
  public void secretUpdate() {
    Secret inspect = secretApi.secretInspect(defaultSecret.getId());
    SecretSpec secretSpec = inspect.getSpec();
    assertNotNull(secretSpec);
    assertDoesNotThrow(() -> secretApi.secretUpdate(defaultSecret.getId(), inspect.getVersion().getIndex(), new SecretSpec(secretSpec.getName(), singletonMap(LABEL_KEY, LABEL_VALUE), secretSpec.getData(), secretSpec.getDriver(), secretSpec.getTemplating())));
  }
}
