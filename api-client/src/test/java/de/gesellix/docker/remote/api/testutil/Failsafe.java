package de.gesellix.docker.remote.api.testutil;

import de.gesellix.docker.remote.api.EngineApiClient;

public class Failsafe {

  public static void removeContainer(EngineApiClient engineApiClient, String container) {
    perform(() -> engineApiClient.getContainerApi().containerStop(container, 5));
    perform(() -> engineApiClient.getContainerApi().containerWait(container));
    perform(() -> engineApiClient.getContainerApi().containerDelete(container, null, null, null));
  }

  public static void perform(Runnable action) {
    try {
      action.run();
    }
    catch (Exception e) {
      System.out.println("ignoring " + e.getMessage());
    }
  }
}
