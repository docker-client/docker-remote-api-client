package de.gesellix.docker.remote.api.testutil;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import de.gesellix.docker.engine.DockerClientConfig;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(EnabledIfSupportsWebSocket.WebSocketCondition.class)
public @interface EnabledIfSupportsWebSocket {

  class WebSocketCondition implements ExecutionCondition {

    public WebSocketCondition() {
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
      return supportsWebSocket()
          ? enabled("Enabled: WebSockets supported")
          : disabled("Disabled: WebSockets not supported");
    }

    public static boolean isUnixSocket() {
      String dockerHost = new DockerClientConfig().getEnv().getDockerHost();
      return dockerHost.startsWith("unix://");
    }

    static boolean isTcpSocket() {
      String dockerHost = new DockerClientConfig().getEnv().getDockerHost();
      return dockerHost.startsWith("tcp://") || dockerHost.startsWith("http://") || dockerHost.startsWith("https://");
    }

    private static boolean supportsWebSocket() {
      if (System.getProperty("os.name").toLowerCase().contains("mac")) {
        // currently not working on macOS
        // see https://github.com/docker/for-mac/issues/1662
        return false;
      }
      return isTcpSocket() || isUnixSocket();
    }
  }
}
