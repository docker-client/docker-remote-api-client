package de.gesellix.docker.remote.api.client;

import de.gesellix.docker.engine.DockerClientConfig;
import de.gesellix.docker.remote.api.BuildInfo;
import de.gesellix.docker.remote.api.ImageID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageApiTest {

  private ImageApi imageApi;

  @BeforeEach
  public void setup() {
    imageApi = new ImageApi(new DockerClientConfig());
  }

  @Test
  public void getImageIdFromAux() {
    List<BuildInfo> infos = new ArrayList<>();
    infos.add(new BuildInfo(null, null, null, null, null, null, null, new ImageID("sha256:expected-id")));
    infos.add(new BuildInfo(null, "Successfully built the wind\ncaught it", null, null, null, null, null, null));
    infos.add(new BuildInfo(null, "Successfully built f9d5f290d048\nfoo bar", null, null, null, null, null, null));
    infos.add(new BuildInfo(null, "Successfully tagged image:tag\nbar baz", null, null, null, null, null, null));

    ImageID imageId = imageApi.getImageId(infos);

    assertEquals("sha256:expected-id", imageId.getID());
  }

  @Test
  public void getImageIdFromStreamWithBuildMessage() {
    List<BuildInfo> infos = new ArrayList<>();
    infos.add(new BuildInfo(null, "Successfully built the wind\ncaught it", null, null, null, null, null, null));
    infos.add(new BuildInfo(null, "Successfully built f9d5f290d048\nfoo bar", null, null, null, null, null, null));
    infos.add(new BuildInfo(null, "Successfully tagged image:tag\nbar baz", null, null, null, null, null, null));

    ImageID imageId = imageApi.getImageId(infos);

    assertEquals("f9d5f290d048", imageId.getID());
  }

  @Test
  public void getImageIdFromStreamWithTagMessage() {
    List<BuildInfo> infos = new ArrayList<>();
    infos.add(new BuildInfo(null, "Successfully tagged image:tag\nbar baz", null, null, null, null, null, null));

    ImageID imageId = imageApi.getImageId(infos);

    assertEquals("image:tag", imageId.getID());
  }
}
