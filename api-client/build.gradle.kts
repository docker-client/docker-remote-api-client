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
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  constraints {
    implementation("com.squareup.moshi:moshi") {
      version {
        strictly("[1.12.0,1.14.0)")
        prefer("1.13.0")
      }
    }
  }
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
  implementation("com.squareup.moshi:moshi:1.13.0")
  implementation("com.squareup.okhttp3:okhttp:[4.9,5)!!4.9.3")
//  implementation("com.squareup.okhttp3:logging-interceptor:[4.9,5)!!4.9.3")
  implementation("de.gesellix:docker-remote-api-model-1-41:2022-02-01T11-07-00")
  implementation("de.gesellix:docker-engine:2022-02-22T23-12-00")
  implementation("de.gesellix:docker-filesocket:2022-02-22T19-32-00")

  implementation("org.slf4j:slf4j-api:[1.7,)!!1.7.32")
  testImplementation("ch.qos.logback:logback-classic:[1.2,2)!!1.2.7")

  testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
  testRuntimeOnly("cglib:cglib-nodep:3.3.0")
  testImplementation("org.junit.platform:junit-platform-launcher:1.8.2")
  testImplementation("org.junit.platform:junit-platform-commons:1.8.2")

  testImplementation("org.apache.commons:commons-compress:1.21")
  testImplementation("de.gesellix:testutil:[2021-08-05T22-09-32,)")
}

tasks.withType(Test::class.java) {
  useJUnitPlatform()
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
