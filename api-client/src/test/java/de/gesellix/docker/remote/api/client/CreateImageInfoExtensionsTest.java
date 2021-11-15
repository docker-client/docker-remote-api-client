package de.gesellix.docker.remote.api.client;

import de.gesellix.docker.remote.api.CreateImageInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateImageInfoExtensionsTest {

  @Test
  public void getImageIdFromStatus() {
    List<CreateImageInfo> infos = new ArrayList<>();
    infos.add(new CreateImageInfo("os-linux", null, "Pulling from gesellix/echo-server", null, null));
    infos.add(new CreateImageInfo(null, null, "Digest: sha256:04c0275878dc243b2f92193467cb33cdb9ee2262be64b627ed476de73e399244", null, null));
    infos.add(new CreateImageInfo(null, null, "Status: Image is up to date for gesellix/echo-server:os-linux", null, null));
    infos.add(new CreateImageInfo(null, null, "sha256:87f5e747ad067f91a7c4adf154deea20cebc3a749be5c2864a0c65cf70ddd8c4", null, null));

    String imageId = CreateImageInfoExtensionsKt.getImageId(infos);

    assertEquals("sha256:87f5e747ad067f91a7c4adf154deea20cebc3a749be5c2864a0c65cf70ddd8c4", imageId);
  }
}
