package de.gesellix.docker.client.core.context;

import java.io.File;
import java.util.Objects;

import de.gesellix.docker.client.core.config.DockerEnv;

public class ContextStore {

  private final MetadataStore metadataStore;

  public ContextStore(File dockerContextStoreDir) {
    File metaRoot = new File(dockerContextStoreDir, MetadataStore.metadataDir);
//    final String tlsDir = "tls";
//    File tlsRoot = new File(env.getDockerContextStoreDir(), tlsDir);
    metadataStore = new MetadataStore(metaRoot);
  }

  public Metadata getMetadata(String contextName) {
    if (Objects.equals(contextName, DockerEnv.dockerDefaultContextName)) {
      // should return the equivalent metadata of `docker context inspect default`
      Metadata metadata = new Metadata(DockerEnv.dockerDefaultContextName);
      metadata.setMetadata(new DockerContext(""));
      metadata.getEndpoints().put(
          DockerEnv.dockerEndpointDefaultName,
          new EndpointMetaBase(DockerEnv.getDockerHostFromSystemPropertyOrEnvironment(), false));
      return metadata;
    }

    return metadataStore.getMetadata(contextName);
  }
}
