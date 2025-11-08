# Complete DNS Resolution Fix Guide for GitHub Windows Runners

## Problem Summary
Integration tests for a Java Docker API client fail intermittently on GitHub Actions `windows-latest` runners with DNS lookup failures and connection aborts when pulling images from `registry-1.docker.io`.

---

## Solution 1: Add Retry Logic with Exponential Backoff ⭐ (Recommended)

**File to modify:** `de.***.docker.remote.api.testutil.TestImage.java`

### Complete Implementation

```java
package de.***.docker.remote.api.testutil;

import de.***.docker.remote.api.client.ImageApi;

public class TestImage {
    private final ImageApi imageApi;
    private final String imageName;
    
    // Configuration constants
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 2000;
    private static final int MAX_RETRY_DELAY_MS = 16000;
    
    public TestImage(ImageApi imageApi, String imageName) {
        this.imageApi = imageApi;
        this.imageName = imageName;
        prepare();
    }
    
    public void prepare() {
        int retryDelay = INITIAL_RETRY_DELAY_MS;
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                System.out.println("Pulling image " + imageName + " (attempt " + attempt + "/" + MAX_RETRIES + ")");
                imageApi.imageCreate(imageName);
                System.out.println("Successfully pulled image " + imageName);
                return; // Success!
                
            } catch (Exception e) {
                lastException = e;
                String message = e.getMessage();
                
                boolean isDnsOrNetworkError = message != null && (
                    message.contains("no such host") ||
                    message.contains("connectex") ||
                    message.contains("dial tcp") ||
                    message.contains("connection was aborted") ||
                    message.contains("i/o timeout")
                );
                
                if (isDnsOrNetworkError && attempt < MAX_RETRIES) {
                    System.err.println("Attempt " + attempt + " failed with network error: " + message);
                    System.err.println("Retrying in " + retryDelay + "ms...");
                    
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while retrying image pull", ie);
                    }
                    
                    // Exponential backoff with cap
                    retryDelay = Math.min(retryDelay * 2, MAX_RETRY_DELAY_MS);
                } else {
                    // Not a network error or out of retries
                    break;
                }
            }
        }
        
        // If we get here, all retries failed
        throw new RuntimeException("Failed to pull image " + imageName + " after " + MAX_RETRIES + " attempts", lastException);
    }
}
```

### Alternative: Kotlin Version (if using Kotlin)

```kotlin
package de.***.docker.remote.api.testutil

import de.***.docker.remote.api.client.ImageApi
import kotlinx.coroutines.delay
import kotlin.math.min

class TestImage(
    private val imageApi: ImageApi,
    private val imageName: String
) {
    companion object {
        private const val MAX_RETRIES = 3
        private const val INITIAL_RETRY_DELAY_MS = 2000L
        private const val MAX_RETRY_DELAY_MS = 16000L
    }
    
    init {
        prepare()
    }
    
    private fun prepare() {
        var retryDelay = INITIAL_RETRY_DELAY_MS
        var lastException: Exception? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                println("Pulling image $imageName (attempt ${attempt + 1}/$MAX_RETRIES)")
                imageApi.imageCreate(imageName)
                println("Successfully pulled image $imageName")
                return
                
            } catch (e: Exception) {
                lastException = e
                val message = e.message ?: ""
                
                val isDnsOrNetworkError = message.contains("no such host") ||
                    message.contains("connectex") ||
                    message.contains("dial tcp") ||
                    message.contains("connection was aborted") ||
                    message.contains("i/o timeout")
                
                if (isDnsOrNetworkError && attempt < MAX_RETRIES - 1) {
                    System.err.println("Attempt ${attempt + 1} failed with network error: $message")
                    System.err.println("Retrying in ${retryDelay}ms...")
                    
                    Thread.sleep(retryDelay)
                    retryDelay = min(retryDelay * 2, MAX_RETRY_DELAY_MS)
                } else {
                    throw RuntimeException("Failed to pull image $imageName after $MAX_RETRIES attempts", lastException)
                }
            }
        }
    }
}
```

---

## Solution 2: Pre-pull Images in GitHub Workflow

**File to modify:** `.github/workflows/your-workflow.yml`

### Complete Workflow Example

```yaml
name: Java CI with Integration Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: windows-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
    
    # Pre-pull Docker images to avoid DNS issues during tests
    - name: Pre-pull Docker images
      run: |
        echo "Pre-pulling Docker images to avoid DNS issues..."
        $images = @(
          "***/echo-server:2025-07-27T22-12-00",
          "***/echo-server:latest"
        )
        
        foreach ($image in $images) {
          echo "Pulling $image..."
          $retries = 3
          $success = $false
          
          for ($i = 1; $i -le $retries; $i++) {
            try {
              docker pull $image
              $success = $true
              echo "Successfully pulled $image"
              break
            } catch {
              echo "Attempt $i failed for $image"
              if ($i -lt $retries) {
                echo "Retrying in 5 seconds..."
                Start-Sleep -Seconds 5
              }
            }
          }
          
          if (-not $success) {
            echo "Warning: Failed to pull $image after $retries attempts"
          }
        }
      shell: powershell
      continue-on-error: true
      timeout-minutes: 10
    
    # Verify images are available
    - name: Verify Docker images
      run: docker images
      shell: powershell
    
    - name: Build with Gradle
      run: ./gradlew build -x test
    
    - name: Run integration tests
      run: ./gradlew integrationTest
      timeout-minutes: 30
```

### Alternative: Simpler Version

```yaml
    - name: Pre-pull Docker images
      run: |
        docker pull ***/echo-server:2025-07-27T22-12-00
        docker pull ***/echo-server:latest
      continue-on-error: true
      timeout-minutes: 5
      shell: powershell
```

---

## Solution 3: Configure Docker DNS Fallback

**File to modify:** `.github/workflows/your-workflow.yml`

### Complete Implementation with Daemon Restart

```yaml
    - name: Configure Docker DNS for stability
      run: |
        echo "Configuring Docker daemon with fallback DNS servers..."
        
        # Stop Docker service
        Stop-Service docker
        
        # Create daemon.json with DNS configuration
        $daemonConfig = @{
          "dns" = @("8.8.8.8", "8.8.4.4", "1.1.1.1")
          "dns-search" = @()
          "max-concurrent-downloads" = 3
          "max-concurrent-uploads" = 3
        } | ConvertTo-Json -Depth 10
        
        $configPath = "C:\ProgramData\docker\config\daemon.json"
        $configDir = Split-Path -Parent $configPath
        
        # Ensure directory exists
        if (-not (Test-Path $configDir)) {
          New-Item -ItemType Directory -Path $configDir -Force
        }
        
        # Write configuration
        $daemonConfig | Out-File -FilePath $configPath -Encoding ASCII -Force
        
        echo "Docker daemon configuration:"
        Get-Content $configPath
        
        # Start Docker service
        Start-Service docker
        
        # Wait for Docker to be ready
        $maxWait = 30
        $waited = 0
        while ($waited -lt $maxWait) {
          try {
            docker info | Out-Null
            echo "Docker is ready"
            break
          } catch {
            echo "Waiting for Docker to start... ($waited seconds)"
            Start-Sleep -Seconds 2
            $waited += 2
          }
        }
        
        # Verify DNS configuration
        docker info
      shell: powershell
      timeout-minutes: 5
```

### Alternative: Linux-style Configuration (if switching to ubuntu-latest)

```yaml
    - name: Configure Docker DNS (Linux)
      run: |
        echo "Configuring Docker daemon with fallback DNS servers..."
        
        sudo mkdir -p /etc/docker
        
        cat <<EOF | sudo tee /etc/docker/daemon.json
        {
          "dns": ["8.8.8.8", "8.8.4.4", "1.1.1.1"],
          "dns-search": [],
          "max-concurrent-downloads": 3,
          "max-concurrent-uploads": 3
        }
        EOF
        
        sudo systemctl restart docker
        
        # Wait for Docker to be ready
        timeout 30 bash -c 'until docker info > /dev/null 2>&1; do sleep 2; done'
        
        echo "Docker daemon configuration:"
        cat /etc/docker/daemon.json
        docker info
      timeout-minutes: 5
```

---

## Solution 4: Add Test-Level Retries with JUnit

**File to modify:** `de.***.docker.remote.api.client.ContainerApiIntegrationTest.java`

### JUnit 5 with @RepeatedTest

```java
package de.***.docker.remote.api.client;

import org.junit.jupiter.api.*;

import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ContainerApiIntegrationTest {
    
    private ImageApi imageApi;
    private TestImage testImage;
    
    @BeforeAll
    public void setup() {
        // Initialize API clients
        this.imageApi = new ImageApi(/* config */);
        this.testImage = new TestImage(imageApi, "***/echo-server:2025-07-27T22-12-00");
    }
    
    @RepeatedTest(value = 3, failureThreshold = 2, name = "Attempt {currentRepetition} of {totalRepetitions}")
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    @DisplayName("Container attach non-interactive should work")
    public void containerAttachNonInteractive() {
        // Your test implementation
    }
    
    @RepeatedTest(value = 3, failureThreshold = 2, name = "Attempt {currentRepetition} of {totalRepetitions}")
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    @DisplayName("Container restart should work")
    public void containerRestart() {
        // Your test implementation
    }
    
    @RepeatedTest(value = 3, failureThreshold = 2, name = "Attempt {currentRepetition} of {totalRepetitions}")
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    @DisplayName("Container inspect missing should handle gracefully")
    public void containerInspectMissing() {
        // Your test implementation
    }
    
    @AfterAll
    public void teardown() {
        // Cleanup
    }
}
```

### Alternative: Custom Retry Extension for JUnit 5

**Create new file:** `RetryOnDnsFailureExtension.java`

```java
package de.***.docker.remote.api.testutil;

import org.junit.jupiter.api.extension.*;

public class RetryOnDnsFailureExtension implements TestExecutionExceptionHandler {
    
    private static final int MAX_RETRIES = 3;
    
    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        String retryCountKey = context.getUniqueId() + "_retryCount";
        ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.create(getClass()));
        
        Integer retryCount = store.get(retryCountKey, Integer.class);
        if (retryCount == null) {
            retryCount = 0;
        }
        
        boolean isDnsError = isDnsOrNetworkError(throwable);
        
        if (isDnsError && retryCount < MAX_RETRIES) {
            retryCount++;
            store.put(retryCountKey, retryCount);
            
            System.err.println("Test failed with DNS/network error (attempt " + retryCount + "/" + MAX_RETRIES + ")");
            System.err.println("Error: " + throwable.getMessage());
            System.err.println("Retrying in 2 seconds...");
            
            try {
                Thread.sleep(2000 * retryCount); // Exponential backoff
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Trigger retry by re-throwing a special exception
            throw new RetryableTestException("Retrying due to DNS/network error", throwable);
        } else {
            // Out of retries or not a DNS error
            throw throwable;
        }
    }
    
    private boolean isDnsOrNetworkError(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null) return false;
        
        return message.contains("no such host") ||
               message.contains("connectex") ||
               message.contains("dial tcp") ||
               message.contains("connection was aborted") ||
               message.contains("i/o timeout");
    }
    
    private static class RetryableTestException extends Exception {
        public RetryableTestException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
```

**Usage in test class:**

```java
@ExtendWith(RetryOnDnsFailureExtension.class)
public class ContainerApiIntegrationTest {
    // Your tests
}
```

### Gradle Configuration for JUnit 5 Retries

**File to modify:** `build.gradle` or `build.gradle.kts`

```gradle
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.10.0'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.10.0'
}

test {
    useJUnitPlatform()
    
    // Global retry configuration
    retry {
        maxRetries = 3
        maxFailures = 10
        failOnPassedAfterRetry = false
    }
    
    // Filter for integration tests
    filter {
        includeTestsMatching "*IntegrationTest"
    }
    
    testLogging {
        events "passed", "skipped", "failed", "standardOut", "standardError"
        showStandardStreams = true
        exceptionFormat = "full"
    }
}
```

---

## Solution 5: Switch to Ubuntu Runners

**File to modify:** `.github/workflows/your-workflow.yml`

### Complete Ubuntu Workflow

```yaml
name: Java CI with Integration Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest  # Changed from windows-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
    
    - name: Configure Docker for stability
      run: |
        # Configure DNS fallback
        sudo mkdir -p /etc/docker
        cat <<EOF | sudo tee /etc/docker/daemon.json
        {
          "dns": ["8.8.8.8", "8.8.4.4"],
          "max-concurrent-downloads": 3
        }
        EOF
        
        sudo systemctl restart docker
        docker info
    
    - name: Build with Gradle
      run: ./gradlew build -x test
    
    - name: Run integration tests
      run: ./gradlew integrationTest
      timeout-minutes: 30
```

### Matrix Strategy for Multi-OS Testing

```yaml
jobs:
  test:
    strategy:
      fail-fast: false
      matrix:
        os: [ubuntu-latest, windows-latest, macos-latest]
        java: [17, 21]
    
    runs-on: ${{ matrix.os }}
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK ${{ matrix.java }}
      uses: actions/setup-java@v4
      with:
        java-version: ${{ matrix.java }}
        distribution: 'temurin'
        cache: gradle
    
    - name: Configure Docker DNS (Windows)
      if: runner.os == 'Windows'
      run: |
        # Windows DNS configuration (see Solution 3)
      shell: powershell
    
    - name: Configure Docker DNS (Linux)
      if: runner.os == 'Linux'
      run: |
        # Linux DNS configuration (see Solution 3)
    
    - name: Run tests
      run: ./gradlew integrationTest
      timeout-minutes: 30
```

---

## Solution 6: Combination Approach (Best Practice) ⭐⭐

### Complete Implementation

**Step 1:** Modify `TestImage.java` with retry logic (Solution 1)

**Step 2:** Update workflow with all stability improvements

```yaml
name: Java CI with Integration Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: windows-latest
    timeout-minutes: 60
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
    
    # Step 1: Configure Docker DNS
    - name: Configure Docker with stable DNS
      run: |
        Stop-Service docker
        
        $config = @{
          "dns" = @("8.8.8.8", "8.8.4.4", "1.1.1.1")
          "max-concurrent-downloads" = 3
        } | ConvertTo-Json
        
        $configPath = "C:\ProgramData\docker\config\daemon.json"
        New-Item -ItemType Directory -Path (Split-Path $configPath) -Force
        $config | Out-File -FilePath $configPath -Encoding ASCII -Force
        
        Start-Service docker
        
        # Wait for Docker
        $waited = 0
        while ($waited -lt 30) {
          try {
            docker info | Out-Null
            break
          } catch {
            Start-Sleep -Seconds 2
            $waited += 2
          }
        }
        
        docker info
      shell: powershell
      timeout-minutes: 5
    
    # Step 2: Pre-pull images
    - name: Pre-pull Docker images
      run: |
        $images = @(
          "***/echo-server:2025-07-27T22-12-00"
        )
        
        foreach ($image in $images) {
          for ($i = 1; $i -le 3; $i++) {
            try {
              docker pull $image
              echo "Pulled $image"
              break
            } catch {
              if ($i -lt 3) {
                Start-Sleep -Seconds (5 * $i)
              }
            }
          }
        }
      shell: powershell
      continue-on-error: true
      timeout-minutes: 10
    
    # Step 3: Build and test
    - name: Build with Gradle
      run: ./gradlew build -x test
    
    - name: Run integration tests
      run: ./gradlew integrationTest --info
      timeout-minutes: 30
    
    # Step 4: Upload logs on failure
    - name: Upload test logs
      if: failure()
      uses: actions/upload-artifact@v4
      with:
        name: test-logs-${{ github.run_number }}
        path: |
          build/reports/tests/
          build/test-results/
        retention-days: 7
```

---

## Recommended Implementation Order

1. **Start with Solution 1** (TestImage retry logic) - this fixes the root cause
2. **Add Solution 2** (pre-pull images) - prevents the problem
3. **If still issues, add Solution 3** (DNS configuration) - network layer fix
4. **Optional: Add Solution 4** (test retries) - safety net
5. **Last resort: Solution 5** (switch to Ubuntu) - if Windows is not required

## Testing Your Changes

After implementing, verify with:

```bash
# Local test
./gradlew integrationTest

# Check Docker connectivity
docker pull ***/echo-server:2025-07-27T22-12-00

# Test with retry simulation
# Add artificial DNS failures to verify retry logic works
```

This comprehensive guide should resolve your DNS issues on GitHub Windows runners!
