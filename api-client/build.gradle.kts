import java.text.SimpleDateFormat
import java.util.*

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("maven-publish")
  id("signing")
  id("com.github.ben-manes.versions")
  id("org.sonatype.gradle.plugins.scan")
  id("io.freefair.maven-central.validate-poms")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
}

dependencies {
  constraints {
    implementation(libs.moshi) {
      version {
        strictly(libs.versions.moshiVersionrange.get())
        prefer(libs.versions.moshi.get())
      }
    }
    listOf(libs.bundles.okio).forEach {
      implementation(it) {
        version {
          strictly(libs.versions.okioVersionrange.get())
          prefer(libs.versions.okio.get())
        }
      }
    }
    implementation(libs.okhttp) {
      version {
        strictly(libs.versions.okhttpVersionrange.get())
        prefer(libs.versions.okhttp.get())
      }
    }
    implementation("de.gesellix:docker-remote-api-model-1-41") {
      version {
        strictly("[2024-01-01T01-01-01,)")
      }
    }
    implementation("de.gesellix:docker-engine") {
      version {
        strictly("[2024-01-01T01-01-01,)")
      }
    }
    implementation("de.gesellix:docker-filesocket") {
      version {
        strictly("[2024-01-01T01-01-01,)")
      }
    }
    listOf(
      "net.java.dev.jna:jna",
      "net.java.dev.jna:jna-platform"
    ).forEach {
      implementation(it) {
        version {
          strictly("[5.0.0,)")
          prefer("5.9.0")
        }
      }
    }
    implementation(libs.slf4j) {
      version {
        strictly(libs.versions.slf4jVersionrange.get())
        prefer(libs.versions.slf4j.get())
      }
    }
  }
  implementation(libs.kotlinJdk8)
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation(libs.moshi)
  implementation(libs.okhttp)
//  implementation("com.squareup.okhttp3:logging-interceptor:[4.9,5)!!4.11.0")
  implementation("de.gesellix:docker-remote-api-model-1-41:2025-10-31T17-49-00")
  implementation("de.gesellix:docker-engine:2025-10-31T17-46-00")
  implementation("de.gesellix:docker-filesocket:2025-10-16T20-25-00")

  implementation(libs.slf4j)
  testImplementation("ch.qos.logback:logback-classic:${libs.versions.logbackVersionrange.get()}!!${libs.versions.logback.get()}")

  testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
  testImplementation(libs.junitJupiterApi)
  testRuntimeOnly(libs.junitJupiterEngine)
  testRuntimeOnly("cglib:cglib-nodep:3.3.0")
  testImplementation(libs.junitPlatformLauncher)
  testImplementation(libs.junitPlatformCommons)

  testImplementation("org.apache.commons:commons-compress:1.28.0")
  testImplementation("de.gesellix:testutil:[2024-01-01T01-01-01,)")
  testImplementation("de.gesellix:docker-registry:2025-10-31T17-45-00")
}

tasks {
  withType<JavaCompile> {
    options.encoding = "UTF-8"
  }
  withType<Test> {
    useJUnitPlatform()
  }
}

val javadocJar by tasks.registering(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("javadoc")
  from(tasks.javadoc)
}

val sourcesJar by tasks.registering(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allSource)
}

artifacts {
  add("archives", sourcesJar.get())
  add("archives", javadocJar.get())
}

fun findProperty(s: String) = project.findProperty(s) as String?

val isSnapshot = project.version == "unspecified"
val artifactVersion = if (!isSnapshot) project.version as String else SimpleDateFormat("yyyy-MM-dd\'T\'HH-mm-ss").format(Date())!!
val publicationName = "dockerRemoteApiClient"
publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/${property("github.package-registry.owner")}/${property("github.package-registry.repository")}")
      credentials {
        username = System.getenv("GITHUB_ACTOR") ?: findProperty("github.package-registry.username")
        password = System.getenv("GITHUB_TOKEN") ?: findProperty("github.package-registry.password")
      }
    }
  }
  publications {
    register(publicationName, MavenPublication::class) {
      pom {
        name.set("docker-remote-api-client")
        description.set("Client for the Docker remote api")
        url.set("https://github.com/docker-client/docker-remote-api-client")
        licenses {
          license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
          }
        }
        developers {
          developer {
            id.set("gesellix")
            name.set("Tobias Gesellchen")
            email.set("tobias@gesellix.de")
          }
        }
        scm {
          connection.set("scm:git:github.com/docker-client/docker-remote-api-client.git")
          developerConnection.set("scm:git:ssh://github.com/docker-client/docker-remote-api-client.git")
          url.set("https://github.com/docker-client/docker-remote-api-client")
        }
      }
      artifactId = "docker-remote-api-client"
      version = artifactVersion
      from(components["java"])
      artifact(sourcesJar.get())
      artifact(javadocJar.get())
    }
  }
}

signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications[publicationName])
}
