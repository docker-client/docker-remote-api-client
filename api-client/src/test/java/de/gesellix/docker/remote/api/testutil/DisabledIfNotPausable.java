package de.gesellix.docker.remote.api.testutil;

import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.EngineApiClientImpl;
import de.gesellix.docker.remote.api.SystemInfo;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisabledIfNotPausable.WindowsContainerPausableCondition.class)
public @interface DisabledIfNotPausable {

  class WindowsContainerPausableCondition implements ExecutionCondition {

    private final EngineApiClient engineApiClient;

    public WindowsContainerPausableCondition() {
      engineApiClient = new EngineApiClientImpl();
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
      return isPausable()
             ? enabled("Enabled: Non-Windows Server daemon detected")
             : disabled("Disabled: cannot pause Windows Server Containers");
    }

    public boolean isPausable() {
      SystemInfo systemInfo = engineApiClient.getSystemApi().systemInfo();
      return !isWindowsDaemon(systemInfo) || !isDaemonIsolationByProcess(systemInfo);
    }

    public boolean isDaemonIsolationByProcess(SystemInfo systemInfo) {
      return systemInfo.getIsolation() == SystemInfo.Isolation.Process;
    }

    public boolean isWindowsDaemon(SystemInfo systemInfo) {
      return Objects.requireNonNull(systemInfo.getOsType())
          .equalsIgnoreCase("windows");
    }
  }
}
