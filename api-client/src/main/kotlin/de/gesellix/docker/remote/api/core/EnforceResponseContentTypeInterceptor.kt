package de.gesellix.docker.remote.api.core

import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection.HTTP_NO_CONTENT

data class EnforceResponseContentTypeConfig(val fallbackContentType: String = "")

// This one would work automatically, when the response content-type would be set correctly :-/
// see https://github.com/gesellix/docker-client/issues/21
// see https://github.com/moby/moby/pull/39812
// - for /attach, /logs and similar endpoints the issue has been fixed with api version 1.42
// - for /stats see (?)
class EnforceResponseContentTypeInterceptor : Interceptor {

  private val log by logger()

  override fun intercept(chain: Interceptor.Chain): Response {
    val response = chain.proceed(chain.request())
    if (chain.request().tag(EnforceResponseContentTypeConfig::class.java)?.fallbackContentType?.isNotEmpty() == true) {
      if (response.isSuccessful && response.code != HTTP_NO_CONTENT && response.headers("Content-Type").isEmpty()) {
        val enforcedContentType = chain.request().tag(EnforceResponseContentTypeConfig::class.java)?.fallbackContentType!!
        log.debug("Overriding Content-Type response header with $enforcedContentType")
        return response.newBuilder().header("Content-Type", enforcedContentType).build()
      }
    }
    return response
  }
}
