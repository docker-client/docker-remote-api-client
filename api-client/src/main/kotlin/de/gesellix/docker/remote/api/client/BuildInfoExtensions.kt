package de.gesellix.docker.remote.api.client

import de.gesellix.docker.remote.api.BuildInfo
import de.gesellix.docker.remote.api.ImageID

fun List<BuildInfo>.getImageId(): ImageID? {
  val reversedInfos = this.reversed()
  val firstAux = reversedInfos.stream()
    .filter { (_, _, _, _, _, _, _, aux): BuildInfo -> aux != null }
    .findFirst()
  if (firstAux.isPresent) {
    return firstAux.get().aux
  } else {
    val idFromStream = reversedInfos.stream()
      .filter { (_, stream): BuildInfo ->
        val contains = stream?.contains("Successfully built ")
        contains != null && contains
      }
      .findFirst()
    return if (idFromStream.isPresent) {
      ImageID(idFromStream.get().stream!!.removePrefix("Successfully built ").replaceAfter('\n', "").trim())
    } else {
      val tagFromStream = reversedInfos.stream()
        .filter { (_, stream): BuildInfo ->
          val contains = stream?.contains("Successfully tagged ")
          contains != null && contains
        }
        .findFirst()
      tagFromStream.map { (_, stream): BuildInfo ->
        ImageID(stream!!.removePrefix("Successfully tagged ").replaceAfter('\n', "").trim())
      }
        .orElse(null)
    }
  }
}

fun List<BuildInfo>.getError(): BuildInfo? {
  return this.stream()
    .filter { (_, _, error): BuildInfo -> error != null }
    .findFirst()
    .orElse(null)
}

fun List<BuildInfo>.hasError(): Boolean {
  return this.stream()
    .anyMatch { (_, _, error): BuildInfo -> error != null }
}
