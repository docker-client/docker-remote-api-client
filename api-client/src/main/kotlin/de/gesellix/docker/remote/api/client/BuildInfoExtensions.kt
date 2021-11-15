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
      .filter { (_, stream): BuildInfo -> stream?.contains("Successfully built ")!! }
      .findFirst()
    return if (idFromStream.isPresent) {
      ImageID(idFromStream.get().stream!!.removePrefix("Successfully built ").replaceAfter('\n', "").trim())
    } else {
      val tagFromStream = reversedInfos.stream()
        .filter { (_, stream): BuildInfo -> stream?.contains("Successfully tagged ")!! }
        .findFirst()
      tagFromStream.map { (_, stream): BuildInfo ->
        ImageID(stream!!.removePrefix("Successfully tagged ").replaceAfter('\n', "").trim())
      }
        .orElse(null)
    }
  }
}
