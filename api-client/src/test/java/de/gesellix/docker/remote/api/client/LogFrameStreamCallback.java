package de.gesellix.docker.remote.api.client;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import de.gesellix.docker.remote.api.core.Cancellable;
import de.gesellix.docker.remote.api.core.Frame;
import de.gesellix.docker.remote.api.core.LoggingExtensionsKt;
import de.gesellix.docker.remote.api.core.StreamCallback;

class LogFrameStreamCallback implements StreamCallback<Frame> {

  private static final Logger log = LoggingExtensionsKt.logger(LogFrameStreamCallback.class.getName()).getValue();

  List<Frame> frames = new ArrayList<>();
  Cancellable job = null;

  @Override
  public void onStarting(Cancellable cancellable) {
    job = cancellable;
  }

  @Override
  public void onNext(Frame frame) {
    frames.add(frame);
    log.info("next: {}", frame);
  }
}
