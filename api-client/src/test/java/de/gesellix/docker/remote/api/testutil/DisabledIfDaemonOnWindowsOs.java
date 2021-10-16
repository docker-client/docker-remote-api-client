package de.gesellix.docker.remote.api.testutil;

import de.gesellix.docker.remote.api.EngineApiClient;
import de.gesellix.docker.remote.api.EngineApiClientImpl;
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
@ExtendWith(DisabledIfDaemonOnWindowsOs.WindowsDaemonCondition.class)
public @interface DisabledIfDaemonOnWindowsOs {

  class WindowsDaemonCondition implements ExecutionCondition {

    private final EngineApiClient engineApiClient;

    public WindowsDaemonCondition() {
      engineApiClient = new EngineApiClientImpl();
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
      return isWindowsDaemon()
             ? disabled("Disabled: Windows daemon detected")
             : enabled("Enabled: Non-Windows daemon detected");
    }

    public boolean isWindowsDaemon() {
      return Objects.requireNonNull(engineApiClient.getSystemApi().systemVersion().getOs())
          .equalsIgnoreCase("windows");
    }
  }
}
