package de.gesellix.docker.client.core.config

import spock.lang.Specification
import spock.lang.Unroll

class DockerVersionSpec extends Specification {

  def "fails for invalid version"() {
    given:
    // this pattern appears in the wild for packages installed from
    // https://master.dockerproject.org/
    String versionString = "master-dockerproject-2022-03-26"

    when:
    DockerVersion.parseDockerVersion(versionString)

    then:
    def e = thrown(IllegalArgumentException)
    e.message == "Version does not match the expected version pattern: '$versionString'"
  }

  @Unroll
  def "parse version #versionString"() {
    expect:
    DockerVersion.parseDockerVersion(versionString) == version
    where:
    versionString    | version
    "1.12.0"         | new DockerVersion(major: 1, minor: 12, patch: 0, meta: "")
    "1.12.0-rc2"     | new DockerVersion(major: 1, minor: 12, patch: 0, meta: "-rc2")
    "17.03.0-ce-rc1" | new DockerVersion(major: 17, minor: 3, patch: 0, meta: "-ce-rc1")
  }

  @Unroll
  def "compare #v1 with #v2"() {
    expect:
    DockerVersion.parseDockerVersion(v1) <=> DockerVersion.parseDockerVersion(v2) == result
    where:
    v1      | v2      || result
    "1.13"  | "17.04" || -1
    "17.05" | "17.04" || 1
    "17.05" | "17.05" || 0
  }
}
