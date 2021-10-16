package de.gesellix.docker.remote.api.testutil;

import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.LocalNodeState;
import de.gesellix.docker.remote.api.SwarmInitRequest;
import de.gesellix.docker.remote.api.client.SwarmApi;
import de.gesellix.docker.remote.api.client.SystemApi;

public class SwarmUtil {

  SwarmApi swarmApi;
  SystemApi systemApi;

  public SwarmUtil(EngineApiClient engineApiClient) {
    this.swarmApi = engineApiClient.getSwarmApi();
    this.systemApi = engineApiClient.getSystemApi();
  }

  public void runWithInactiveSwarm(Runnable action) {
    LocalNodeState previousState = ensureInactiveSwarm();
    try {
      action.run();
    }
    finally {
      if (previousState != LocalNodeState.Inactive) {
        ensureActiveSwarm();
      }
    }
  }

  public void runWithActiveSwarm(Runnable action) {
    LocalNodeState previousState = ensureActiveSwarm();
    try {
      action.run();
    }
    finally {
      if (previousState != LocalNodeState.Active) {
        ensureInactiveSwarm();
      }
    }
  }

  LocalNodeState ensureInactiveSwarm() {
    LocalNodeState currentState = null;
    try {
      currentState = systemApi.systemInfo().getSwarm().getLocalNodeState();
      if (currentState != LocalNodeState.Inactive) {
        swarmApi.swarmLeave(true);
      }
    }
    catch (Exception ignored) {
      //
    }
    return currentState;
  }

  LocalNodeState ensureActiveSwarm() {
    LocalNodeState currentState = null;
    try {
      currentState = systemApi.systemInfo().getSwarm().getLocalNodeState();
      if (currentState != LocalNodeState.Active) {
        swarmApi.swarmInit(new SwarmInitRequest("0.0.0.0:2377", "127.0.0.1", null, null, null, false, null, null));
      }
    }
    catch (Exception ignored) {
      //
    }
    return currentState;
  }
}
