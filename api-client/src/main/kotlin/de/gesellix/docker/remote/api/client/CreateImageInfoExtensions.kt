package de.gesellix.docker.remote.api.client

import de.gesellix.docker.remote.api.CreateImageInfo

fun List<CreateImageInfo>.getImageId(): String? {
  val reversedInfos = this.reversed()
  val firstStatus = reversedInfos.stream()
    .filter { (_, _, _, status, _, _): CreateImageInfo -> status != null }
    .findFirst()
  if (firstStatus.isPresent) {
    return firstStatus.get().status
  } else {
    return null
  }
}
