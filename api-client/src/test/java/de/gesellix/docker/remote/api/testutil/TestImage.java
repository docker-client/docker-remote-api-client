package de.gesellix.docker.remote.api.testutil;

import de.gesellix.docker.remote.api.EngineApiClient;

import java.util.Objects;

public class TestImage {

  private final EngineApiClient engineApiClient;
  private final String repository;
  private final String tag;

  public TestImage(EngineApiClient engineApiClient) {
    this.engineApiClient = engineApiClient;

    boolean isWindows = Objects.requireNonNull(engineApiClient.getSystemApi().systemVersion().getOs()).equalsIgnoreCase("windows");
    this.repository = "gesellix/echo-server";
    this.tag = isWindows ? "os-windows" : "os-linux";

    // TODO consider NOT calling prepare inside the constructor
    prepare();
  }

  public void prepare() {
    engineApiClient.getImageApi().imageCreate(getImageName(), null, null, getImageTag(), null, null, null, null, null);
  }

  public String getImageWithTag() {
    return getImageName() + ":" + getImageTag();
  }

  public String getImageName() {
    return repository;
  }

  public String getImageTag() {
    return tag;
  }
}
