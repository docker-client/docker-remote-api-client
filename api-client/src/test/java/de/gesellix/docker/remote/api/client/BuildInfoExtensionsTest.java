package de.gesellix.docker.remote.api.client;

import de.gesellix.docker.remote.api.BuildInfo;
import de.gesellix.docker.remote.api.ErrorDetail;
import de.gesellix.docker.remote.api.ImageID;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildInfoExtensionsTest {

  @Test
  public void getErrorFromFailedBuild() {
    ErrorDetail errorDetail = new ErrorDetail(null, "invalid reference format");
    BuildInfo buildInfo = new BuildInfo();
    buildInfo.setError(errorDetail.getMessage());
    buildInfo.setErrorDetail(errorDetail);
    List<BuildInfo> infos = new ArrayList<>();
    infos.add(buildInfo);

    BuildInfo errorInfo = BuildInfoExtensionsKt.getError(infos);

    assertEquals("invalid reference format", errorInfo.getErrorDetail().getMessage());
  }

  @Test
  public void getHasErrorInFailedBuild() {
    ErrorDetail errorDetail = new ErrorDetail(null, "invalid reference format");
    BuildInfo buildInfo = new BuildInfo();
    buildInfo.setError(errorDetail.getMessage());
    buildInfo.setErrorDetail(errorDetail);
    List<BuildInfo> infos = new ArrayList<>();
    infos.add(buildInfo);

    boolean hasError = BuildInfoExtensionsKt.hasError(infos);

    assertTrue(hasError);
  }

  @Test
  public void getHasNoErrorInSuccessfulBuild() {
    BuildInfo buildInfo = new BuildInfo();
    buildInfo.setStream("Successfully built the wind\ncaught it");
    List<BuildInfo> infos = new ArrayList<>();
    infos.add(buildInfo);

    boolean hasError = BuildInfoExtensionsKt.hasError(infos);

    assertFalse(hasError);
  }

  @Test
  public void getImageIdFromFailedBuild() {
    ErrorDetail errorDetail = new ErrorDetail(null, "invalid reference format");
    BuildInfo buildInfo = new BuildInfo();
    buildInfo.setError(errorDetail.getMessage());
    buildInfo.setErrorDetail(errorDetail);
    List<BuildInfo> infos = new ArrayList<>();
    infos.add(buildInfo);

    ImageID imageId = BuildInfoExtensionsKt.getImageId(infos);

    assertNull(imageId);
  }

  @Test
  public void getImageIdFromAux() {
    BuildInfo buildInfo1 = new BuildInfo();
    buildInfo1.setAux(new ImageID("sha256:expected-id"));
    BuildInfo buildInfo2 = new BuildInfo();
    buildInfo2.setStream("Successfully built the wind\ncaught it");;
    BuildInfo buildInfo3 = new BuildInfo();
    buildInfo3.setStream("Successfully built f9d5f290d048\nfoo bar");
    BuildInfo buildInfo4 = new BuildInfo();
    buildInfo4.setStream("Successfully tagged image:tag\nbar baz");
    List<BuildInfo> infos = new ArrayList<>();
    infos.add(buildInfo1);
    infos.add(buildInfo2);
    infos.add(buildInfo3);
    infos.add(buildInfo4);

    ImageID imageId = BuildInfoExtensionsKt.getImageId(infos);

    assertEquals("sha256:expected-id", imageId.getID());
  }

  @Test
  public void getImageIdFromStreamWithBuildMessage() {
    BuildInfo buildInfo1 = new BuildInfo();
    buildInfo1.setStream("Successfully built the wind\ncaught it");
    BuildInfo buildInfo2 = new BuildInfo();
    buildInfo2.setStream("Successfully built f9d5f290d048\nfoo bar");
    BuildInfo buildInfo3 = new BuildInfo();
    buildInfo3.setStream("Successfully tagged image:tag\nbar baz");
    List<BuildInfo> infos = new ArrayList<>();
    infos.add(buildInfo1);
    infos.add(buildInfo2);
    infos.add(buildInfo3);

    ImageID imageId = BuildInfoExtensionsKt.getImageId(infos);

    assertEquals("f9d5f290d048", imageId.getID());
  }

  @Test
  public void getImageIdFromStreamWithTagMessage() {
    BuildInfo buildInfo = new BuildInfo();
    buildInfo.setStream("Successfully tagged image:tag\nbar baz");
    List<BuildInfo> infos = new ArrayList<>();
    infos.add(buildInfo);

    ImageID imageId = BuildInfoExtensionsKt.getImageId(infos);

    assertEquals("image:tag", imageId.getID());
  }
}
