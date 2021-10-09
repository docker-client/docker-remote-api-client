package de.gesellix.docker.remote.api.core

class LoggingCallback : StreamCallback<Any?> {

  private val log by logger()

  var job: Cancellable? = null
  override fun onStarting(cancellable: Cancellable) {
    job = cancellable
  }

  override fun onNext(event: Any?) {
    log.info("$event")
  }
}
