rootProject.name = "docker-remote-api-client"
include("api-client")

// https://docs.gradle.org/current/userguide/toolchains.html#sub:download_repositories
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}
