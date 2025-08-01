package de.gesellix.docker.remote.api.core;

import okio.Sink;

public interface StreamCallback<T> {

  default void onStarting(Cancellable cancellable) {
  }

  default void attachInput(Sink sink) {
    try {
      Thread.sleep(500);
      sink.close();
    } catch (Exception ignored) {
    }
    throw new IllegalStateException("Falling back to default implementation that closes the sink after 500ms. This is probably not what you want. Please provide a custom implementation of this method to handle the input stream.");
  }

  void onNext(T element);

  default void onFailed(Exception e) {
    throw new RuntimeException(e);
  }

  default void onFinished() {
  }
}
