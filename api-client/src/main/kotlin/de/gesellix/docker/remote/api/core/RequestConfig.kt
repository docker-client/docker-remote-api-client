package de.gesellix.docker.remote.api.core

import de.gesellix.docker.engine.RequestMethod
import java.lang.reflect.Type
import kotlin.time.Duration

/**
 * Defines a config object for a given request.
 * NOTE: This object doesn't include 'body' because it
 *       allows for caching of the constructed object
 *       for many request definitions.
 * NOTE: Headers is a Map<String,String> because rfc2616 defines
 *       multi-valued headers as csv-only.
 */
data class RequestConfig(
  val method: RequestMethod,
  val path: String,
  val headers: MutableMap<String, String> = mutableMapOf(),
  val query: MutableMap<String, List<String>> = mutableMapOf(),
  val body: Any? = null,
  val elementType: Type? = null,
  val apiVersion: String? = null,
  val timeout: Duration = Duration.ZERO
)
