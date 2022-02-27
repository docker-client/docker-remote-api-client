package de.gesellix.docker.remote.api.client;

import com.squareup.moshi.Moshi;
import de.gesellix.docker.builder.BuildContextBuilder;
import de.gesellix.docker.registry.DockerRegistry;
import de.gesellix.docker.remote.api.BuildInfo;
import de.gesellix.docker.remote.api.BuildPruneResponse;
import de.gesellix.docker.remote.api.ContainerCreateRequest;
import de.gesellix.docker.remote.api.ContainerCreateResponse;
import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.HistoryResponseItem;
import de.gesellix.docker.remote.api.IdResponse;
import de.gesellix.docker.remote.api.Image;
import de.gesellix.docker.remote.api.ImageDeleteResponseItem;
import de.gesellix.docker.remote.api.ImageID;
import de.gesellix.docker.remote.api.ImageSearchResponseItem;
import de.gesellix.docker.remote.api.ImageSummary;
import de.gesellix.docker.remote.api.core.StreamCallback;
import de.gesellix.docker.remote.api.testutil.DockerEngineAvailable;
import de.gesellix.docker.remote.api.testutil.HttpTestServer;
import de.gesellix.docker.remote.api.testutil.InjectDockerClient;
import de.gesellix.docker.remote.api.testutil.ManifestUtil;
import de.gesellix.docker.remote.api.testutil.NetworkInterfaces;
import de.gesellix.docker.remote.api.testutil.TarUtil;
import de.gesellix.docker.remote.api.testutil.TestImage;
import de.gesellix.testutil.ResourceReader;
import okio.Okio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_KEY;
import static de.gesellix.docker.remote.api.testutil.Constants.LABEL_VALUE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DockerEngineAvailable
class ImageApiIntegrationTest {

  private static Logger log = LoggerFactory.getLogger(ImageApiIntegrationTest.class);

  @InjectDockerClient
  private EngineApiClient engineApiClient;

  private TestImage testImage;

  ImageApi imageApi;
  ContainerApi containerApi;

  @BeforeEach
  public void setup() {
    imageApi = engineApiClient.getImageApi();
    containerApi = engineApiClient.getContainerApi();
    testImage = new TestImage(engineApiClient);
  }

  @Test
  public void buildPrune() {
    BuildPruneResponse response = imageApi.buildPrune(null, null, null);
    assertTrue(response.getSpaceReclaimed() >= 0);
  }

  @Test
  public void imageBuildAndPrune() throws IOException {
    imageApi.imageTag(testImage.getImageWithTag(), "test", "build-base");
    String dockerfile = "/images/builder/Dockerfile";
    File inputDirectory = ResourceReader.getClasspathResourceAsFile(dockerfile, ImageApi.class).getParentFile();
    InputStream buildContext = newBuildContext(inputDirectory);

    List<BuildInfo> infos = new ArrayList<>();
    Duration timeout = Duration.of(1, ChronoUnit.MINUTES);
    CountDownLatch latch = new CountDownLatch(1);
    StreamCallback<BuildInfo> callback = new StreamCallback<BuildInfo>() {
      @Override
      public void onNext(BuildInfo element) {
        log.info(element.toString());
        infos.add(element);
      }

      @Override
      public void onFailed(Exception e) {
        log.error("Build failed", e);
        latch.countDown();
      }

      @Override
      public void onFinished() {
        latch.countDown();
      }
    };
    assertDoesNotThrow(() -> imageApi.imageBuild(Paths.get(dockerfile).getFileName().toString(), "test:build", null, null, null, null, null, null,
                                                 null, null, null, null, null, null, null,
                                                 null, null, null, null, null, null, null,
                                                 null, null, null, null, buildContext,
                                                 callback, timeout.toMillis()));
    try {
      latch.await(2, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      log.error("Wait interrupted", e);
    }

    ImageID imageId = BuildInfoExtensionsKt.getImageId(infos);
    assertNotNull(imageId);
    assertNotNull(imageId.getID());
    assertTrue(imageId.getID().matches(".+"));

    Map<String, List<String>> filter = new HashMap<>();
    filter.put("label", singletonList(LABEL_KEY));
    String filterJson = new Moshi.Builder().build().adapter(Map.class).toJson(filter);
    assertDoesNotThrow(() -> imageApi.imagePrune(filterJson));

    imageApi.imageDelete("test:build", null, null);
    imageApi.imageDelete("test:build-base", null, null);
  }

  InputStream newBuildContext(File baseDirectory) throws IOException {
    ByteArrayOutputStream buildContext = new ByteArrayOutputStream();
    BuildContextBuilder.archiveTarFilesRecursively(baseDirectory, buildContext);
    return new ByteArrayInputStream(buildContext.toByteArray());
  }

  @Test
  public void imageCreatePullFromRemote() {
    assertDoesNotThrow(() -> imageApi.imageCreate(testImage.getImageName(), null, null, testImage.getImageTag(), null, null, null, null, null));
  }

  @Test
  public void imageCreateImportFromUrl() throws IOException {
    InputStream tarFile = imageApi.imageGet(testImage.getImageWithTag());
    File destDir = new TarUtil().unTar(tarFile);
    File rootLayerTar = new ManifestUtil().getRootLayerLocation(destDir);
    URL importUrl = rootLayerTar.toURI().toURL();
    HttpTestServer server = new HttpTestServer();
    InetSocketAddress serverAddress = server.start("/images/", new HttpTestServer.FileServer(importUrl));
    int port = serverAddress.getPort();
    List<String> addresses = new NetworkInterfaces().getInet4Addresses();
    String url = String.format("http://%s:%s/images/%s", addresses.get(0), port, importUrl.getPath());

    assertDoesNotThrow(() -> imageApi.imageCreate(null, url, "test", "from-url", null, null, singletonList(String.format("LABEL %1$s=\"%2$s\"", LABEL_KEY, LABEL_VALUE)), null, null));

    server.stop();
    imageApi.imageDelete("test:from-url", null, null);
  }

  @Test
  public void imageCreateImportFromInputStream() throws IOException {
    InputStream tarFile = imageApi.imageGet(testImage.getImageWithTag());
    File destDir = new TarUtil().unTar(tarFile);
    File rootLayerTar = new ManifestUtil().getRootLayerLocation(destDir);
    try (InputStream source = new FileInputStream(rootLayerTar)) {
      assertDoesNotThrow(() -> imageApi.imageCreate(null, "-", "test", "from-stream", null, null, singletonList(String.format("LABEL %1$s=\"%2$s\"", LABEL_KEY, LABEL_VALUE)), null, source));
    }
    imageApi.imageDelete("test:from-stream", null, null);
  }

  @Test
  public void imageCommit() {
    imageApi.imageTag(testImage.getImageWithTag(), "test", "commit");
    ContainerCreateRequest containerCreateRequest = new ContainerCreateRequest(
        null, null, null,
        false, false, false,
        null,
        false, null, null,
        null,
        singletonList("-"),
        null,
        null,
        "test:commit",
        null, null, null,
        null, null,
        null,
        singletonMap(LABEL_KEY, LABEL_VALUE),
        null, null,
        null,
        null,
        null
    );
    ContainerCreateResponse container = containerApi.containerCreate(containerCreateRequest, "container-commit-test");
    IdResponse image = imageApi.imageCommit(container.getId(), "test", "committed", null, null, null, null, null);
    assertTrue(image.getId().matches("sha256:\\w+"));
    imageApi.imageDelete("test:committed", null, null);
    containerApi.containerDelete("container-commit-test", null, null, null);
    imageApi.imageDelete("test:commit", null, null);
  }

  @Test
  public void imageList() {
    List<ImageSummary> images = imageApi.imageList(null, null, null);
    assertEquals(1, images.stream().filter((i) -> i.getRepoTags() != null && i.getRepoTags().stream().filter((t) -> t.equals(testImage.getImageWithTag())).count() > 0).count());
  }

  @Test
  public void imageDelete() {
    imageApi.imageTag(testImage.getImageWithTag(), "test", "delete");
    List<ImageDeleteResponseItem> deletedImages = imageApi.imageDelete("test:delete", null, null);
    assertTrue(deletedImages.stream().anyMatch((e) -> e.getDeleted() != null || e.getUntagged() != null));
    assertDoesNotThrow(() -> imageApi.imageDelete("image-delete-missing", null, null));
  }

  @Test
  public void imageGet() throws IOException {
    imageApi.imageTag(testImage.getImageWithTag(), "test", "export");
    InputStream exportedImage = imageApi.imageGet("test:export");
    long byteCount = Okio.buffer(Okio.source(exportedImage)).readAll(Okio.blackhole());
    exportedImage.close();
    assertTrue(16896 < byteCount);

    imageApi.imageDelete("test:export", null, null);
  }

  @Test
  public void imageGetAll() throws IOException {
    imageApi.imageTag(testImage.getImageWithTag(), "test", "export-all-1");
    imageApi.imageTag(testImage.getImageWithTag(), "test", "export-all-2");

    InputStream exportedImages = imageApi.imageGetAll(asList("test:export-all-1", "test:export-all-2"));
    long byteCount = Okio.buffer(Okio.source(exportedImages)).readAll(Okio.blackhole());
    exportedImages.close();
    assertTrue(22016 < byteCount);

    imageApi.imageDelete("test:export-all-1", null, null);
    imageApi.imageDelete("test:export-all-2", null, null);
  }

  @Test
  public void imageLoad() {
    List<String> originalRepoDigests = imageApi.imageInspect(testImage.getImageWithTag()).getRepoDigests();

    imageApi.imageTag(testImage.getImageWithTag(), "test", "load-image");
    InputStream tarFile = imageApi.imageGet("test:load-image");
    imageApi.imageDelete("test:load-image", null, null);

    assertDoesNotThrow(() -> imageApi.imageLoad(false, tarFile));

    List<String> actualRepoDigests = imageApi.imageInspect("test:load-image").getRepoDigests();
    assertEquals(originalRepoDigests, actualRepoDigests);

    imageApi.imageDelete("test:load-image", null, null);
  }

  @Test
  public void imageHistory() {
    imageApi.imageTag(testImage.getImageWithTag(), "test", "history");

    List<HistoryResponseItem> history = imageApi.imageHistory("test:history");
    assertFalse(history.isEmpty());
    Optional<HistoryResponseItem> historyItem = history.stream().filter(h -> h.getCreatedBy().contains("ENTRYPOINT [\"/main\"")).findFirst();
    assertTrue(historyItem.isPresent());

    imageApi.imageDelete("test:history", null, null);
  }

  @Test
  public void imageInspect() {
    imageApi.imageTag(testImage.getImageWithTag(), "test", "inspect");

    Image image = imageApi.imageInspect("test:inspect");
    assertTrue(image.getId().startsWith("sha256:"));

    imageApi.imageDelete("test:inspect", null, null);
  }

  @Test
  public void imageSearch() {
    List<ImageSearchResponseItem> searchResult = imageApi.imageSearch("alpine", 1, null);
    assertEquals(1, searchResult.size());
    assertEquals("alpine", searchResult.get(0).getName());
  }

  @Test
  public void imageTag() {
    imageApi.imageTag(testImage.getImageWithTag(), "test/image", "test-tag");
    Image image1 = imageApi.imageInspect(testImage.getImageWithTag());
    Image image2 = imageApi.imageInspect("test/image:test-tag");
    assertFalse(image1.getId().isEmpty());
    assertEquals(image1.getId(), image2.getId());

    imageApi.imageDelete("test/image:test-tag", null, null);
  }

  @Test
  public void imagePushToCustomRegistry() {
    DockerRegistry registry = new DockerRegistry();
    registry.run();
    String registryUrl = registry.url();

    imageApi.imageTag(testImage.getImageWithTag(), registryUrl + "/test", "push");

    imageApi.imagePush(registryUrl + "/test", "", "push");

    registry.rm();

    imageApi.imageDelete(registryUrl + "/test:push", null, null);
  }
}
