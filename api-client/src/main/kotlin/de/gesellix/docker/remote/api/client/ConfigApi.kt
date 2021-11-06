package de.gesellix.docker.remote.api.client

import de.gesellix.docker.engine.DockerClientConfig
import de.gesellix.docker.engine.RequestMethod
import de.gesellix.docker.remote.api.Config
import de.gesellix.docker.remote.api.ConfigSpec
import de.gesellix.docker.remote.api.IdResponse
import de.gesellix.docker.remote.api.core.ApiClient
import de.gesellix.docker.remote.api.core.ClientError
import de.gesellix.docker.remote.api.core.ClientException
import de.gesellix.docker.remote.api.core.MultiValueMap
import de.gesellix.docker.remote.api.core.RequestConfig
import de.gesellix.docker.remote.api.core.ResponseType
import de.gesellix.docker.remote.api.core.ServerError
import de.gesellix.docker.remote.api.core.ServerException
import de.gesellix.docker.remote.api.core.Success
import java.net.Proxy

class ConfigApi(dockerClientConfig: DockerClientConfig = defaultClientConfig, proxy: Proxy?) : ApiClient(dockerClientConfig, proxy) {
  constructor(dockerClientConfig: DockerClientConfig = defaultClientConfig) : this(dockerClientConfig, null)

  companion object {

    @JvmStatic
    val defaultClientConfig: DockerClientConfig by lazy {
      DockerClientConfig()
    }
  }

  /**
   * Create a config
   *
   * @param body  (optional)
   * @return IdResponse
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Suppress("UNCHECKED_CAST")
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun configCreate(body: ConfigSpec?): IdResponse {
    val localVariableConfig = configCreateRequestConfig(body = body)

    val localVarResponse = request<IdResponse>(
      localVariableConfig
    )

    return when (localVarResponse.responseType) {
      ResponseType.Success -> (localVarResponse as Success<*>).data as IdResponse
      ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
      ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
      ResponseType.ClientError -> {
        val localVarError = localVarResponse as ClientError<*>
        throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
      }
      ResponseType.ServerError -> {
        val localVarError = localVarResponse as ServerError<*>
        throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
      }
    }
  }

  /**
   * To obtain the request config of the operation configCreate
   *
   * @param body  (optional)
   * @return RequestConfig
   */
  fun configCreateRequestConfig(body: ConfigSpec?): RequestConfig {
    val localVariableBody: Any? = body
    val localVariableQuery: MultiValueMap = mutableMapOf()
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = RequestMethod.POST,
      path = "/configs/create",
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }

  /**
   * Delete a config
   *
   * @param id ID of the config
   * @return void
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun configDelete(id: String) {
    val localVariableConfig = configDeleteRequestConfig(id = id)

    val localVarResponse = request<Any?>(
      localVariableConfig
    )

    return when (localVarResponse.responseType) {
      ResponseType.Success -> Unit
      ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
      ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
      ResponseType.ClientError -> {
        val localVarError = localVarResponse as ClientError<*>
        throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
      }
      ResponseType.ServerError -> {
        val localVarError = localVarResponse as ServerError<*>
        throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
      }
    }
  }

  /**
   * To obtain the request config of the operation configDelete
   *
   * @param id ID of the config
   * @return RequestConfig
   */
  fun configDeleteRequestConfig(id: String): RequestConfig {
    val localVariableBody: Any? = null
    val localVariableQuery: MultiValueMap = mutableMapOf()
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = RequestMethod.DELETE,
      path = "/configs/{id}".replace("{" + "id" + "}", id),
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }

  /**
   * Inspect a config
   *
   * @param id ID of the config
   * @return Config
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Suppress("UNCHECKED_CAST")
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun configInspect(id: String): Config {
    val localVariableConfig = configInspectRequestConfig(id = id)

    val localVarResponse = request<Config>(
      localVariableConfig
    )

    return when (localVarResponse.responseType) {
      ResponseType.Success -> (localVarResponse as Success<*>).data as Config
      ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
      ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
      ResponseType.ClientError -> {
        val localVarError = localVarResponse as ClientError<*>
        throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
      }
      ResponseType.ServerError -> {
        val localVarError = localVarResponse as ServerError<*>
        throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
      }
    }
  }

  /**
   * To obtain the request config of the operation configInspect
   *
   * @param id ID of the config
   * @return RequestConfig
   */
  fun configInspectRequestConfig(id: String): RequestConfig {
    val localVariableBody: Any? = null
    val localVariableQuery: MultiValueMap = mutableMapOf()
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = RequestMethod.GET,
      path = "/configs/{id}".replace("{" + "id" + "}", id),
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }

  /**
   * List configs
   *
   * @param filters A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the configs list.  Available filters:  - &#x60;id&#x3D;&lt;config id&gt;&#x60; - &#x60;label&#x3D;&lt;key&gt; or label&#x3D;&lt;key&gt;&#x3D;value&#x60; - &#x60;name&#x3D;&lt;config name&gt;&#x60; - &#x60;names&#x3D;&lt;config name&gt;&#x60;  (optional)
   * @return kotlin.collections.List<Config>
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Suppress("UNCHECKED_CAST")
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun configList(filters: String?): List<Config> {
    val localVariableConfig = configListRequestConfig(filters = filters)

    val localVarResponse = request<List<Config>>(
      localVariableConfig
    )

    return when (localVarResponse.responseType) {
      ResponseType.Success -> (localVarResponse as Success<*>).data as List<Config>
      ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
      ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
      ResponseType.ClientError -> {
        val localVarError = localVarResponse as ClientError<*>
        throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
      }
      ResponseType.ServerError -> {
        val localVarError = localVarResponse as ServerError<*>
        throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
      }
    }
  }

  /**
   * To obtain the request config of the operation configList
   *
   * @param filters A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the configs list.  Available filters:  - &#x60;id&#x3D;&lt;config id&gt;&#x60; - &#x60;label&#x3D;&lt;key&gt; or label&#x3D;&lt;key&gt;&#x3D;value&#x60; - &#x60;name&#x3D;&lt;config name&gt;&#x60; - &#x60;names&#x3D;&lt;config name&gt;&#x60;  (optional)
   * @return RequestConfig
   */
  fun configListRequestConfig(filters: String?): RequestConfig {
    val localVariableBody: Any? = null
    val localVariableQuery: MultiValueMap = mutableMapOf<String, List<String>>()
      .apply {
        if (filters != null) {
          put("filters", listOf(filters.toString()))
        }
      }
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = RequestMethod.GET,
      path = "/configs",
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody,
      elementType = Config::class.java
    )
  }

  /**
   * Update a Config
   *
   * @param id The ID or name of the config
   * @param version The version number of the config object being updated. This is required to avoid conflicting writes.
   * @param body The spec of the config to update. Currently, only the Labels field can be updated. All other fields must remain unchanged from the [ConfigInspect endpoint](#operation/ConfigInspect) response values.  (optional)
   * @return void
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun configUpdate(id: String, version: Long, body: ConfigSpec?) {
    val localVariableConfig = configUpdateRequestConfig(id = id, version = version, body = body)

    val localVarResponse = request<Any?>(
      localVariableConfig
    )

    return when (localVarResponse.responseType) {
      ResponseType.Success -> Unit
      ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
      ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
      ResponseType.ClientError -> {
        val localVarError = localVarResponse as ClientError<*>
        throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
      }
      ResponseType.ServerError -> {
        val localVarError = localVarResponse as ServerError<*>
        throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
      }
    }
  }

  /**
   * To obtain the request config of the operation configUpdate
   *
   * @param id The ID or name of the config
   * @param version The version number of the config object being updated. This is required to avoid conflicting writes.
   * @param body The spec of the config to update. Currently, only the Labels field can be updated. All other fields must remain unchanged from the [ConfigInspect endpoint](#operation/ConfigInspect) response values.  (optional)
   * @return RequestConfig
   */
  fun configUpdateRequestConfig(id: String, version: Long, body: ConfigSpec?): RequestConfig {
    val localVariableBody: Any? = body
    val localVariableQuery: MultiValueMap = mutableMapOf<String, List<String>>()
      .apply {
        put("version", listOf(version.toString()))
      }
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = RequestMethod.POST,
      path = "/configs/{id}/update".replace("{" + "id" + "}", id),
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }
}
