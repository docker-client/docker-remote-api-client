package de.gesellix.docker.remote.api.core

import java.io.IOException

interface Reader<T> {
  @Throws(IOException::class)
  fun readNext(type: Class<T>): T

  @Throws(IOException::class)
  fun hasNext(): Boolean
}
