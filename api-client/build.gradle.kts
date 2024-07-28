import java.text.SimpleDateFormat
import java.util.*

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("maven-publish")
  id("signing")
  id("com.github.ben-manes.versions")
  id("net.ossindex.audit")
  id("io.freefair.maven-central.validate-poms")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
}

dependencies {
  constraints {
    implementation("com.squareup.moshi:moshi") {
      version {
        strictly("[1.12.0,2)")
        prefer("1.15.1")
      }
    }
    listOf(
      "com.squareup.okio:okio",
      "com.squareup.okio:okio-jvm"
    ).forEach {
      implementation(it) {
        version {
          strictly("[3,4)")
          prefer("3.9.0")
        }
      }
    }
    implementation("com.squareup.okhttp3:okhttp") {
      version {
        strictly("[4.9,5)")
        prefer("4.12.0")
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
    implementation("org.slf4j:slf4j-api") {
      version {
        strictly("[1.7,3)")
        prefer("2.0.13")
      }
    }
  }
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.23")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
  implementation("com.squareup.moshi:moshi:1.15.1")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
//  implementation("com.squareup.okhttp3:logging-interceptor:[4.9,5)!!4.11.0")
  implementation("de.gesellix:docker-remote-api-model-1-41:2024-07-23T23-55-00")
  implementation("de.gesellix:docker-engine:2024-07-28T18-40-00")
  implementation("de.gesellix:docker-filesocket:2024-07-23T22-35-00")

  implementation("org.slf4j:slf4j-api:2.0.13")
  testImplementation("ch.qos.logback:logback-classic:[1.2,2)!!1.3.14")

  testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.3")
  testRuntimeOnly("cglib:cglib-nodep:3.3.0")
  testImplementation("org.junit.platform:junit-platform-launcher:1.10.3")
  testImplementation("org.junit.platform:junit-platform-commons:1.10.3")

  testImplementation("org.apache.commons:commons-compress:1.26.2")
  testImplementation("de.gesellix:testutil:[2024-01-01T01-01-01,)")
  testImplementation("de.gesellix:docker-registry:2024-03-14T19-35-00")
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
