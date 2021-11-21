package de.gesellix.docker.remote.api.testutil;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AwaitUtil {

  public boolean await(Callable<Boolean> task, Duration timeout) throws InterruptedException {
    CountDownLatch wait = new CountDownLatch(1);
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        try {
          Boolean result = task.call();
          if (result) {
            wait.countDown();
            timer.cancel();
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }, 500, 500);
    return wait.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
  }
}
