package de.gesellix.docker.client.core.authentication;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gesellix.docker.client.core.config.DockerConfigReader;
import de.gesellix.docker.client.core.config.DockerEnv;

public class AuthConfigReader {

  private final static Logger log = LoggerFactory.getLogger(AuthConfigReader.class);

  private final DockerEnv env;
  private DockerConfigReader dockerConfigReader;

  public AuthConfigReader() {
    this(new DockerEnv());
  }

  public AuthConfigReader(DockerEnv env) {
    this.env = env;
    this.dockerConfigReader = env.getDockerConfigReader();
  }

  //  @Override
  public AuthConfig readDefaultAuthConfig() {
    return readAuthConfig(null, dockerConfigReader.getDockerConfigFile());
  }

  //  @Override
  public AuthConfig readAuthConfig(String hostname, File dockerCfg) {
    log.debug("read authConfig");

    if (hostname == null || hostname.trim().isEmpty()) {
      hostname = env.getIndexUrl_v1();
    }

    Map parsedDockerCfg = dockerConfigReader.readDockerConfigFile(dockerCfg);
    if (parsedDockerCfg == null || parsedDockerCfg.isEmpty()) {
      return AuthConfig.EMPTY_AUTH_CONFIG;
    }

    de.gesellix.docker.client.core.authentication.CredsStore credsStore = getCredentialsStore(parsedDockerCfg, hostname);
    return credsStore.getAuthConfig(hostname);
  }

  public de.gesellix.docker.client.core.authentication.CredsStore getCredentialsStore(Map parsedDockerCfg) {
    return getCredentialsStore(parsedDockerCfg, "");
  }

  public CredsStore getCredentialsStore(Map parsedDockerCfg, String hostname) {
    if (parsedDockerCfg.containsKey("credHelpers") && hostname != null && !hostname.trim().isEmpty()) {
      return new de.gesellix.docker.client.core.authentication.NativeStore((String) ((Map) parsedDockerCfg.get("credHelpers")).get(hostname));
    }
    if (parsedDockerCfg.containsKey("credsStore")) {
      return new NativeStore((String) parsedDockerCfg.get("credsStore"));
    }
    return new FileStore(parsedDockerCfg);
  }
}
