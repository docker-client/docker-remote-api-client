package de.gesellix.docker.remote.api.client;

import com.squareup.moshi.Moshi;
import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.Volume;
import de.gesellix.docker.remote.api.VolumeConfig;
import de.gesellix.docker.remote.api.VolumePruneResponse;
import de.gesellix.docker.remote.api.testutil.DockerEngineAvailable;
import de.gesellix.docker.remote.api.testutil.InjectDockerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_KEY;
import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_VALUE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DockerEngineAvailable
class VolumeApiIntegrationTest {

  @InjectDockerClient
  private EngineApiClient engineApiClient;

  VolumeApi volumeApi;

  String fileSeparator;

  @BeforeEach
  public void setup() {
    volumeApi = engineApiClient.getVolumeApi();
    boolean isWindowsDaemon = Objects.requireNonNull(engineApiClient.getSystemApi().systemVersion().getOs()).equalsIgnoreCase("windows");
    fileSeparator = isWindowsDaemon ? "\\" : "/";
  }

  @Test
  public void volumeCreate() {
    Volume volume = volumeApi.volumeCreate(new VolumeConfig("my-volume", null, Collections.emptyMap(), Collections.emptyMap()));
    assertTrue(volume.getMountpoint().endsWith(fileSeparator + "my-volume" + fileSeparator + "_data"));
    volumeApi.volumeDelete(volume.getName(), false);
  }

  @Test
  public void volumeDelete() {
    Volume volume = volumeApi.volumeCreate(new VolumeConfig("my-volume", null, Collections.emptyMap(), Collections.emptyMap()));
    assertDoesNotThrow(() -> volumeApi.volumeDelete(volume.getName(), false));
  }

  @Test
  public void volumeInspect() {
    volumeApi.volumeCreate(new VolumeConfig("my-volume", null, Collections.emptyMap(), Collections.emptyMap()));
    Volume volume = volumeApi.volumeInspect("my-volume");
    assertTrue(volume.getMountpoint().endsWith(fileSeparator + "my-volume" + fileSeparator + "_data"));
    volumeApi.volumeDelete(volume.getName(), false);
  }

  @Test
  public void volumeList() {
    Volume volume = volumeApi.volumeCreate(new VolumeConfig("my-volume", null, Collections.emptyMap(), Collections.emptyMap()));
    Optional<Volume> myVolume = volumeApi.volumeList(null).getVolumes().stream().filter((v) -> v.getName().equals(volume.getName())).findFirst();
    assertEquals(volume.getMountpoint(), myVolume.orElse(new Volume("none", "none", "none", Collections.emptyMap(), Volume.Scope.Local, Collections.emptyMap(), "none", Collections.emptyMap(), null)).getMountpoint());
    volumeApi.volumeDelete(volume.getName(), false);
  }

  @Test
  public void volumePrune() {
    Map<String, List<String>> filter = new HashMap<>();
    filter.put("label", Collections.singletonList(LABEL_KEY));
    String filterJson = new Moshi.Builder().build().adapter(Map.class).toJson(filter);

    Volume volume = volumeApi.volumeCreate(new VolumeConfig("my-volume", null, Collections.emptyMap(), Collections.singletonMap(LABEL_KEY, LABEL_VALUE)));
    VolumePruneResponse pruneResponse = volumeApi.volumePrune(filterJson);
    assertTrue(Objects.requireNonNull(pruneResponse.getVolumesDeleted()).stream().allMatch((v) -> v.equals(volume.getName())));
  }
}
