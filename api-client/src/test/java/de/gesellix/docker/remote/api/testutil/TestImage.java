package de.gesellix.docker.remote.api.testutil;

import de.gesellix.docker.remote.api.EngineApiClient;

import java.util.Objects;

public class TestImage {

  private final EngineApiClient engineApiClient;
  private final String repository;
  private final String tag;
  private final boolean useWindowsContainer;

  public TestImage(EngineApiClient engineApiClient) {
    this.engineApiClient = engineApiClient;

    this.useWindowsContainer = Objects.requireNonNull(engineApiClient.getSystemApi().systemVersion().getOs()).equalsIgnoreCase("windows");
    this.repository = "gesellix/echo-server";
    this.tag = "2024-12-22T16-35-00";

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

  public boolean isWindowsContainer() {
    return useWindowsContainer;
  }
}
