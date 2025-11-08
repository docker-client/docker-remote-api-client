package de.gesellix.docker.remote.api.core

import de.gesellix.docker.client.core.config.DockerClientConfig
import java.net.Proxy

class GenericApi(dockerClientConfig: DockerClientConfig = defaultClientConfig, proxy: Proxy?) : ApiClient(dockerClientConfig, proxy) {
    constructor(dockerClientConfig: DockerClientConfig = defaultClientConfig) : this(dockerClientConfig, null)
  companion object {

    @JvmStatic
    val defaultClientConfig: DockerClientConfig by lazy {
      DockerClientConfig()
    }
  }

  fun httpRequest(request: RequestConfig): ApiInfrastructureResponse<Any?> {
    return request<Any>(
      request
    )
  }
}
