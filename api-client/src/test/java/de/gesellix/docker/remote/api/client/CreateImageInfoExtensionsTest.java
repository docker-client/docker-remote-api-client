package de.gesellix.docker.remote.api.client;

import de.gesellix.docker.remote.api.CreateImageInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateImageInfoExtensionsTest {

  @Test
  public void getImageIdFromStatus() {
    CreateImageInfo imageInfo1 = new CreateImageInfo();
    imageInfo1.setId("example");
    imageInfo1.setStatus("Pulling from gesellix/echo-server");
    CreateImageInfo imageInfo2 = new CreateImageInfo();
    imageInfo2.setStatus("Digest: sha256:04c0275878dc243b2f92193467cb33cdb9ee2262be64b627ed476de73e399244");
    CreateImageInfo imageInfo3 = new CreateImageInfo();
    imageInfo3.setStatus("Status: Image is up to date for gesellix/echo-server:example");
    CreateImageInfo imageInfo4 = new CreateImageInfo();
    imageInfo4.setStatus("sha256:87f5e747ad067f91a7c4adf154deea20cebc3a749be5c2864a0c65cf70ddd8c4");
    List<CreateImageInfo> infos = new ArrayList<>();
    infos.add(imageInfo1);
    infos.add(imageInfo2);
    infos.add(imageInfo3);
    infos.add(imageInfo4);

    String imageId = CreateImageInfoExtensionsKt.getImageId(infos);

    assertEquals("sha256:87f5e747ad067f91a7c4adf154deea20cebc3a749be5c2864a0c65cf70ddd8c4", imageId);
  }
}
