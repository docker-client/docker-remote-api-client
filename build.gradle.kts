import java.text.SimpleDateFormat
import java.util.*

rootProject.extra.set("artifactVersion", SimpleDateFormat("yyyy-MM-dd\'T\'HH-mm-ss").format(Date()))

plugins {
  id("maven-publish")
  id("com.github.ben-manes.versions") version "0.49.0"
  id("net.ossindex.audit") version "0.4.11"
  id("io.freefair.maven-central.validate-poms") version "8.4"
  id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
  id("org.jetbrains.kotlin.jvm") version "1.9.10" apply false
}

val dependencyVersions = listOf(
  "commons-io:commons-io:2.14.0",
  "org.apiguardian:apiguardian-api:1.1.2",
  "org.jetbrains:annotations:24.0.1",
  "org.jetbrains.kotlin:kotlin-reflect:1.9.10",
  "org.jetbrains.kotlin:kotlin-scripting-jvm:1.9.10",
  "org.jetbrains.kotlin:kotlin-stdlib:1.9.10",
  "org.jetbrains.kotlin:kotlin-stdlib-common:1.9.10",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.10",
  "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.10",
  "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3",
  "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3",
  "org.junit:junit-bom:5.10.0",
  "org.junit.jupiter:junit-jupiter-api:5.10.0",
  "org.opentest4j:opentest4j:1.3.0"
)

val dependencyVersionsByGroup = mapOf(
  "org.junit.platform" to "1.10.0",
)

subprojects {
  repositories {
//    mavenLocal()
//    fun findProperty(s: String) = project.findProperty(s) as String?
//    maven {
//      name = "github"
//      setUrl("https://maven.pkg.github.com/docker-client/*")
//      credentials {
//        username = System.getenv("PACKAGE_REGISTRY_USER") ?: findProperty("github.package-registry.username")
//        password = System.getenv("PACKAGE_REGISTRY_TOKEN") ?: findProperty("github.package-registry.password")
//      }
//    }
    mavenCentral()
  }
}

allprojects {
  configurations.all {
    resolutionStrategy {
      failOnVersionConflict()
      force(dependencyVersions)
      eachDependency {
        val forcedVersion = dependencyVersionsByGroup[requested.group]
        if (forcedVersion != null) {
          useVersion(forcedVersion)
        }
      }
    }
  }
}

fun findProperty(s: String) = project.findProperty(s) as String?

val isSnapshot = project.version == "unspecified"
nexusPublishing {
  repositories {
    if (!isSnapshot) {
      sonatype {
        // 'sonatype' is pre-configured for Sonatype Nexus (OSSRH) which is used for The Central Repository
        stagingProfileId.set(System.getenv("SONATYPE_STAGING_PROFILE_ID") ?: findProperty("sonatype.staging.profile.id")) //can reduce execution time by even 10 seconds
        username.set(System.getenv("SONATYPE_USERNAME") ?: findProperty("sonatype.username"))
        password.set(System.getenv("SONATYPE_PASSWORD") ?: findProperty("sonatype.password"))
      }
    }
  }
}
