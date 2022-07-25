/**
 * Docker Engine API
 *
 * The Engine API is an HTTP API served by Docker Engine. It is the API the Docker client uses to communicate with the Engine, so everything the Docker client can do can be done with the API.  Most of the client's commands map directly to API endpoints (e.g. `docker ps` is `GET /containers/json`). The notable exception is running containers, which consists of several API calls.  # Errors  The API uses standard HTTP status codes to indicate the success or failure of the API call. The body of the response will be JSON in the following format:  ``` {   \"message\": \"page not found\" } ```  # Versioning  The API is usually changed in each release, so API calls are versioned to ensure that clients don't break. To lock to a specific version of the API, you prefix the URL with its version, for example, call `/v1.30/info` to use the v1.30 version of the `/info` endpoint. If the API version specified in the URL is not supported by the daemon, a HTTP `400 Bad Request` error message is returned.  If you omit the version-prefix, the current version of the API (v1.41) is used. For example, calling `/info` is the same as calling `/v1.41/info`. Using the API without a version-prefix is deprecated and will be removed in a future release.  Engine releases in the near future should support this version of the API, so your client will continue to work even if it is talking to a newer Engine.  The API uses an open schema model, which means server may add extra properties to responses. Likewise, the server will ignore any extra query parameters and request body properties. When you write clients, you need to ignore additional properties in responses to ensure they do not break when talking to newer daemons.   # Authentication  Authentication for registries is handled client side. The client has to send authentication details to various endpoints that need to communicate with registries, such as `POST /images/(name)/push`. These are sent as `X-Registry-Auth` header as a [base64url encoded](https://tools.ietf.org/html/rfc4648#section-5) (JSON) string with the following structure:  ``` {   \"username\": \"string\",   \"password\": \"string\",   \"email\": \"string\",   \"serveraddress\": \"string\" } ```  The `serveraddress` is a domain/IP without a protocol. Throughout this structure, double quotes are required.  If you have already got an identity token from the [`/auth` endpoint](#operation/SystemAuth), you can just pass this instead of credentials:  ``` {   \"identitytoken\": \"9cbaf023786cd7...\" } ```
 *
 * The version of the OpenAPI document: 1.41
 *
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
package de.gesellix.docker.remote.api.client

import de.gesellix.docker.engine.DockerClientConfig
import de.gesellix.docker.engine.RequestMethod.DELETE
import de.gesellix.docker.engine.RequestMethod.GET
import de.gesellix.docker.engine.RequestMethod.POST
import de.gesellix.docker.remote.api.Plugin
import de.gesellix.docker.remote.api.PluginPrivilege
import de.gesellix.docker.remote.api.core.ApiClient
import de.gesellix.docker.remote.api.core.ClientError
import de.gesellix.docker.remote.api.core.ClientException
import de.gesellix.docker.remote.api.core.MultiValueMap
import de.gesellix.docker.remote.api.core.RequestConfig
import de.gesellix.docker.remote.api.core.ResponseType
import de.gesellix.docker.remote.api.core.ServerError
import de.gesellix.docker.remote.api.core.ServerException
import de.gesellix.docker.remote.api.core.Success
import okio.Source
import okio.source
import java.io.InputStream
import java.net.Proxy

class PluginApi(dockerClientConfig: DockerClientConfig = defaultClientConfig, proxy: Proxy?) : ApiClient(dockerClientConfig, proxy) {
  constructor(dockerClientConfig: DockerClientConfig = defaultClientConfig) : this(dockerClientConfig, null)

  companion object {

    @JvmStatic
    val defaultClientConfig: DockerClientConfig by lazy {
      DockerClientConfig()
    }
  }

  /**
   * Get plugin privileges
   *
   * @param remote The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @return kotlin.collections.List<PluginPrivilege>
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun getPluginPrivileges(remote: String): List<PluginPrivilege> {
    val localVariableConfig = getPluginPrivilegesRequestConfig(remote = remote)

    val localVarResponse = request<List<PluginPrivilege>>(
      localVariableConfig
    )

    return when (localVarResponse.responseType) {
      ResponseType.Success -> (localVarResponse as Success<*>).data as List<PluginPrivilege>
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
   * To obtain the request config of the operation getPluginPrivileges
   *
   * @param remote The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @return RequestConfig
   */
  fun getPluginPrivilegesRequestConfig(remote: String): RequestConfig {
    val localVariableBody = null
    val localVariableQuery: MultiValueMap = mutableMapOf<String, List<String>>()
      .apply {
        put("remote", listOf(remote))
      }
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = GET,
      path = "/plugins/privileges",
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody,
      elementType = PluginPrivilege::class.java
    )
  }

  /**
   * Create a plugin
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @param tarContext Path to tar containing plugin rootfs and manifest (optional)
   * @return void
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun pluginCreate(name: String, tarContext: InputStream?) {
    val localVariableConfig = pluginCreateRequestConfig(name = name, tarContext = tarContext)

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
   * To obtain the request config of the operation pluginCreate
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @param tarContext Path to tar containing plugin rootfs and manifest (optional)
   * @return RequestConfig
   */
  fun pluginCreateRequestConfig(name: String, tarContext: InputStream?): RequestConfig {
    val localVariableBody = tarContext?.source()
    val localVariableQuery: MultiValueMap = mutableMapOf<String, List<String>>()
      .apply {
        put("name", listOf(name))
      }
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = POST,
      path = "/plugins/create",
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }

  /**
   * Remove a plugin
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @param force Disable the plugin before removing. This may result in issues if the plugin is in use by a container.  (optional, default to false)
   * @return Plugin
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun pluginDelete(name: String, force: Boolean?): Plugin {
    val localVariableConfig = pluginDeleteRequestConfig(name = name, force = force)

    val localVarResponse = request<Plugin>(
      localVariableConfig
    )

    return when (localVarResponse.responseType) {
      ResponseType.Success -> (localVarResponse as Success<*>).data as Plugin
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
   * To obtain the request config of the operation pluginDelete
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @param force Disable the plugin before removing. This may result in issues if the plugin is in use by a container.  (optional, default to false)
   * @return RequestConfig
   */
  fun pluginDeleteRequestConfig(name: String, force: Boolean?): RequestConfig {
    val localVariableBody = null
    val localVariableQuery: MultiValueMap = mutableMapOf<String, List<String>>()
      .apply {
        if (force != null) {
          put("force", listOf(force.toString()))
        }
      }
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = DELETE,
      path = "/plugins/{name}".replace("{" + "name" + "}", name),
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }

  /**
   * Disable a plugin
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @return void
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun pluginDisable(name: String) {
    val localVariableConfig = pluginDisableRequestConfig(name = name)

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
   * To obtain the request config of the operation pluginDisable
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @return RequestConfig
   */
  fun pluginDisableRequestConfig(name: String): RequestConfig {
    val localVariableBody = null
    val localVariableQuery: MultiValueMap = mutableMapOf()
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = POST,
      path = "/plugins/{name}/disable".replace("{" + "name" + "}", name),
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }

  /**
   * Enable a plugin
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @param timeout Set the HTTP client timeout (in seconds) (optional, default to 0)
   * @return void
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun pluginEnable(name: String, timeout: Int?) {
    val localVariableConfig = pluginEnableRequestConfig(name = name, timeout = timeout)

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
   * To obtain the request config of the operation pluginEnable
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @param timeout Set the HTTP client timeout (in seconds) (optional, default to 0)
   * @return RequestConfig
   */
  fun pluginEnableRequestConfig(name: String, timeout: Int?): RequestConfig {
    val localVariableBody = null
    val localVariableQuery: MultiValueMap = mutableMapOf<String, List<String>>()
      .apply {
        if (timeout != null) {
          put("timeout", listOf(timeout.toString()))
        }
      }
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = POST,
      path = "/plugins/{name}/enable".replace("{" + "name" + "}", name),
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }

  /**
   * Inspect a plugin
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @return Plugin
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun pluginInspect(name: String): Plugin {
    val localVariableConfig = pluginInspectRequestConfig(name = name)

    val localVarResponse = request<Plugin>(
      localVariableConfig
    )

    return when (localVarResponse.responseType) {
      ResponseType.Success -> (localVarResponse as Success<*>).data as Plugin
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
   * To obtain the request config of the operation pluginInspect
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @return RequestConfig
   */
  fun pluginInspectRequestConfig(name: String): RequestConfig {
    val localVariableBody = null
    val localVariableQuery: MultiValueMap = mutableMapOf()
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = GET,
      path = "/plugins/{name}/json".replace("{" + "name" + "}", name),
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }

  /**
   * List plugins
   * Returns information about installed plugins.
   * @param filters A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the plugin list.  Available filters:  - &#x60;capability&#x3D;&lt;capability name&gt;&#x60; - &#x60;enable&#x3D;&lt;true&gt;|&lt;false&gt;&#x60;  (optional)
   * @return kotlin.collections.List<Plugin>
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun pluginList(filters: String?): List<Plugin> {
    val localVariableConfig = pluginListRequestConfig(filters = filters)

    val localVarResponse = request<List<Plugin>>(
      localVariableConfig
    )

    return when (localVarResponse.responseType) {
      ResponseType.Success -> (localVarResponse as Success<*>).data as List<Plugin>
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
   * To obtain the request config of the operation pluginList
   *
   * @param filters A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the plugin list.  Available filters:  - &#x60;capability&#x3D;&lt;capability name&gt;&#x60; - &#x60;enable&#x3D;&lt;true&gt;|&lt;false&gt;&#x60;  (optional)
   * @return RequestConfig
   */
  fun pluginListRequestConfig(filters: String?): RequestConfig {
    val localVariableBody = null
    val localVariableQuery: MultiValueMap = mutableMapOf<String, List<String>>()
      .apply {
        if (filters != null) {
          put("filters", listOf(filters.toString()))
        }
      }
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = GET,
      path = "/plugins",
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody,
      elementType = Plugin::class.java
    )
  }

  /**
   * Install a plugin
   * Pulls and installs a plugin. After the plugin is installed, it can be enabled using the [&#x60;POST /plugins/{name}/enable&#x60; endpoint](#operation/PostPluginsEnable).
   * @param remote Remote reference for plugin to install.  The &#x60;:latest&#x60; tag is optional, and is used as the default if omitted.
   * @param name Local name for the pulled plugin.  The &#x60;:latest&#x60; tag is optional, and is used as the default if omitted.  (optional)
   * @param xRegistryAuth A base64url-encoded auth configuration to use when pulling a plugin from a registry.  Refer to the [authentication section](#section/Authentication) for details.  (optional)
   * @param body  (optional)
   * @return void
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun pluginPull(remote: String, name: String?, xRegistryAuth: String?, body: List<PluginPrivilege>?) {
    val localVariableConfig = pluginPullRequestConfig(remote = remote, name = name, xRegistryAuth = xRegistryAuth, body = body)

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
   * To obtain the request config of the operation pluginPull
   *
   * @param remote Remote reference for plugin to install.  The &#x60;:latest&#x60; tag is optional, and is used as the default if omitted.
   * @param name Local name for the pulled plugin.  The &#x60;:latest&#x60; tag is optional, and is used as the default if omitted.  (optional)
   * @param xRegistryAuth A base64url-encoded auth configuration to use when pulling a plugin from a registry.  Refer to the [authentication section](#section/Authentication) for details.  (optional)
   * @param body  (optional)
   * @return RequestConfig
   */
  fun pluginPullRequestConfig(remote: String, name: String?, xRegistryAuth: String?, body: List<PluginPrivilege>?): RequestConfig {
    val localVariableBody = body
    val localVariableQuery: MultiValueMap = mutableMapOf<String, List<String>>()
      .apply {
        put("remote", listOf(remote))
        if (name != null) {
          put("name", listOf(name.toString()))
        }
      }
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
    xRegistryAuth?.apply { localVariableHeaders["X-Registry-Auth"] = this }

    return RequestConfig(
      method = POST,
      path = "/plugins/pull",
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }

  /**
   * Push a plugin
   * Push a plugin to the registry.
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @return void
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun pluginPush(name: String) {
    val localVariableConfig = pluginPushRequestConfig(name = name)

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
   * To obtain the request config of the operation pluginPush
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @return RequestConfig
   */
  fun pluginPushRequestConfig(name: String): RequestConfig {
    val localVariableBody = null
    val localVariableQuery: MultiValueMap = mutableMapOf()
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = POST,
      path = "/plugins/{name}/push".replace("{" + "name" + "}", name),
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }

  /**
   * Configure a plugin
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @param body  (optional)
   * @return void
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun pluginSet(name: String, body: List<String>?) {
    val localVariableConfig = pluginSetRequestConfig(name = name, body = body)

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
   * To obtain the request config of the operation pluginSet
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @param body  (optional)
   * @return RequestConfig
   */
  fun pluginSetRequestConfig(name: String, body: List<String>?): RequestConfig {
    val localVariableBody = body
    val localVariableQuery: MultiValueMap = mutableMapOf()
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = POST,
      path = "/plugins/{name}/set".replace("{" + "name" + "}", name),
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }

  /**
   * Upgrade a plugin
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @param remote Remote reference to upgrade to.  The &#x60;:latest&#x60; tag is optional, and is used as the default if omitted.
   * @param xRegistryAuth A base64url-encoded auth configuration to use when pulling a plugin from a registry.  Refer to the [authentication section](#section/Authentication) for details.  (optional)
   * @param body  (optional)
   * @return void
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun pluginUpgrade(name: String, remote: String, xRegistryAuth: String?, body: List<PluginPrivilege>?) {
    val localVariableConfig = pluginUpgradeRequestConfig(name = name, remote = remote, xRegistryAuth = xRegistryAuth, body = body)

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
   * To obtain the request config of the operation pluginUpgrade
   *
   * @param name The name of the plugin. The &#x60;:latest&#x60; tag is optional, and is the default if omitted.
   * @param remote Remote reference to upgrade to.  The &#x60;:latest&#x60; tag is optional, and is used as the default if omitted.
   * @param xRegistryAuth A base64url-encoded auth configuration to use when pulling a plugin from a registry.  Refer to the [authentication section](#section/Authentication) for details.  (optional)
   * @param body  (optional)
   * @return RequestConfig
   */
  fun pluginUpgradeRequestConfig(name: String, remote: String, xRegistryAuth: String?, body: List<PluginPrivilege>?): RequestConfig {
    val localVariableBody = body
    val localVariableQuery: MultiValueMap = mutableMapOf<String, List<String>>()
      .apply {
        put("remote", listOf(remote))
      }
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
    xRegistryAuth?.apply { localVariableHeaders["X-Registry-Auth"] = this }

    return RequestConfig(
      method = POST,
      path = "/plugins/{name}/upgrade".replace("{" + "name" + "}", name),
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }
}
