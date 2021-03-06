package de.gesellix.docker.remote.api.core

class LoggingCallback<T> : StreamCallback<T?> {

  private val log by logger()

  var job: Cancellable? = null
  override fun onStarting(cancellable: Cancellable) {
    job = cancellable
  }

  override fun onNext(event: T?) {
    log.info("$event")
  }
}
