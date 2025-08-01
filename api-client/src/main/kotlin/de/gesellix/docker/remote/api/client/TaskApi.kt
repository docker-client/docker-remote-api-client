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
import de.gesellix.docker.engine.RequestMethod.GET
import de.gesellix.docker.remote.api.Task
import de.gesellix.docker.remote.api.core.ApiClient
import de.gesellix.docker.remote.api.core.ClientError
import de.gesellix.docker.remote.api.core.ClientException
import de.gesellix.docker.remote.api.core.Frame
import de.gesellix.docker.remote.api.core.MultiValueMap
import de.gesellix.docker.remote.api.core.RequestConfig
import de.gesellix.docker.remote.api.core.ResponseType
import de.gesellix.docker.remote.api.core.ServerError
import de.gesellix.docker.remote.api.core.ServerException
import de.gesellix.docker.remote.api.core.StreamCallback
import de.gesellix.docker.remote.api.core.Success
import de.gesellix.docker.remote.api.core.SuccessStream
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.net.Proxy

class TaskApi(dockerClientConfig: DockerClientConfig = defaultClientConfig, proxy: Proxy?) : ApiClient(dockerClientConfig, proxy) {
  constructor(dockerClientConfig: DockerClientConfig = defaultClientConfig) : this(dockerClientConfig, null)

  companion object {

    @JvmStatic
    val defaultClientConfig: DockerClientConfig by lazy {
      DockerClientConfig()
    }
  }

  /**
   * Inspect a task
   *
   * @param id ID of the task
   * @return Task
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun taskInspect(id: String): Task {
    val localVariableConfig = taskInspectRequestConfig(id = id)

    val localVarResponse = request<Task>(
      localVariableConfig
    )

    return when (localVarResponse.responseType) {
      ResponseType.Success -> (localVarResponse as Success<*>).data as Task
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
   * To obtain the request config of the operation taskInspect
   *
   * @param id ID of the task
   * @return RequestConfig
   */
  fun taskInspectRequestConfig(id: String): RequestConfig {
    val localVariableBody = null
    val localVariableQuery: MultiValueMap = mutableMapOf()
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = GET,
      path = "/tasks/{id}".replace("{" + "id" + "}", id),
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }

  /**
   * List tasks
   *
   * @param filters A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the tasks list.  Available filters:  - &#x60;desired-state&#x3D;(running | shutdown | accepted)&#x60; - &#x60;id&#x3D;&lt;task id&gt;&#x60; - &#x60;label&#x3D;key&#x60; or &#x60;label&#x3D;\&quot;key&#x3D;value\&quot;&#x60; - &#x60;name&#x3D;&lt;task name&gt;&#x60; - &#x60;node&#x3D;&lt;node id or name&gt;&#x60; - &#x60;service&#x3D;&lt;service name&gt;&#x60;  (optional)
   * @return kotlin.collections.List<Task>
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun taskList(filters: String?): List<Task> {
    val localVariableConfig = taskListRequestConfig(filters = filters)

    val localVarResponse = request<List<Task>>(
      localVariableConfig
    )

    return when (localVarResponse.responseType) {
      ResponseType.Success -> (localVarResponse as Success<List<Task>>).data
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
   * To obtain the request config of the operation taskList
   *
   * @param filters A JSON encoded value of the filters (a &#x60;map[string][]string&#x60;) to process on the tasks list.  Available filters:  - &#x60;desired-state&#x3D;(running | shutdown | accepted)&#x60; - &#x60;id&#x3D;&lt;task id&gt;&#x60; - &#x60;label&#x3D;key&#x60; or &#x60;label&#x3D;\&quot;key&#x3D;value\&quot;&#x60; - &#x60;name&#x3D;&lt;task name&gt;&#x60; - &#x60;node&#x3D;&lt;node id or name&gt;&#x60; - &#x60;service&#x3D;&lt;service name&gt;&#x60;  (optional)
   * @return RequestConfig
   */
  fun taskListRequestConfig(filters: String?): RequestConfig {
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
      path = "/tasks",
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody,
      elementType = Task::class.java
    )
  }

  /**
   * Get task logs
   * Get &#x60;stdout&#x60; and &#x60;stderr&#x60; logs from a task. See also [&#x60;/containers/{id}/logs&#x60;](#operation/ContainerLogs).  **Note**: This endpoint works only for services with the &#x60;local&#x60;, &#x60;json-file&#x60; or &#x60;journald&#x60; logging drivers.
   * @param id ID of the task
   * @param details Show task context and extra details provided to logs. (optional, default to false)
   * @param follow Keep connection after returning logs. (optional, default to false)
   * @param stdout Return logs from &#x60;stdout&#x60; (optional, default to false)
   * @param stderr Return logs from &#x60;stderr&#x60; (optional, default to false)
   * @param since Only return logs since this time, as a UNIX timestamp (optional, default to 0)
   * @param timestamps Add timestamps to every log line (optional, default to false)
   * @param tail Only return this number of log lines from the end of the logs. Specify as an integer or &#x60;all&#x60; to output all log lines.  (optional, default to "all")
   * @return void
   * @throws UnsupportedOperationException If the API returns an informational or redirection response
   * @throws ClientException If the API returns a client error response
   * @throws ServerException If the API returns a server error response
   */
  @Throws(UnsupportedOperationException::class, ClientException::class, ServerException::class)
  fun taskLogs(
    id: String,
    details: Boolean?,
    follow: Boolean?,
    stdout: Boolean?,
    stderr: Boolean?,
    since: Int?,
    timestamps: Boolean?,
    tail: String? = "all",
    callback: StreamCallback<Frame>, timeoutMillis: Long /*= 24.hours.toLongMilliseconds()*/
  ) {
    val localVariableConfig = taskLogsRequestConfig(id = id, details = details, follow = follow, stdout = stdout, stderr = stderr, since = since, timestamps = timestamps, tail = tail)

    val localVarResponse = requestFrames(
      localVariableConfig
    )

    when (localVarResponse.responseType) {
      ResponseType.Success -> {
        runBlocking {
          launch {
            withTimeout(timeoutMillis) {
              callback.onStarting(this@launch::cancel)
              (localVarResponse as SuccessStream<Frame>).data.collect { callback.onNext(it) }
              callback.onFinished()
            }
          }
        }
      }
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
   * To obtain the request config of the operation taskLogs
   *
   * @param id ID of the task
   * @param details Show task context and extra details provided to logs. (optional, default to false)
   * @param follow Keep connection after returning logs. (optional, default to false)
   * @param stdout Return logs from &#x60;stdout&#x60; (optional, default to false)
   * @param stderr Return logs from &#x60;stderr&#x60; (optional, default to false)
   * @param since Only return logs since this time, as a UNIX timestamp (optional, default to 0)
   * @param timestamps Add timestamps to every log line (optional, default to false)
   * @param tail Only return this number of log lines from the end of the logs. Specify as an integer or &#x60;all&#x60; to output all log lines.  (optional, default to "all")
   * @return RequestConfig
   */
  fun taskLogsRequestConfig(
    id: String,
    details: Boolean?,
    follow: Boolean?,
    stdout: Boolean?,
    stderr: Boolean?,
    since: Int?,
    timestamps: Boolean?,
    tail: String?
  ): RequestConfig {
    val localVariableBody = null
    val localVariableQuery: MultiValueMap = mutableMapOf<String, List<String>>()
      .apply {
        if (details != null) {
          put("details", listOf(details.toString()))
        }
        if (follow != null) {
          put("follow", listOf(follow.toString()))
        }
        if (stdout != null) {
          put("stdout", listOf(stdout.toString()))
        }
        if (stderr != null) {
          put("stderr", listOf(stderr.toString()))
        }
        if (since != null) {
          put("since", listOf(since.toString()))
        }
        if (timestamps != null) {
          put("timestamps", listOf(timestamps.toString()))
        }
        if (tail != null) {
          put("tail", listOf(tail.toString()))
        }
      }
    val localVariableHeaders: MutableMap<String, String> = mutableMapOf()

    return RequestConfig(
      method = GET,
      path = "/tasks/{id}/logs".replace("{" + "id" + "}", id),
      query = localVariableQuery,
      headers = localVariableHeaders,
      body = localVariableBody
    )
  }
}
