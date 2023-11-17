package de.gesellix.docker.remote.api.testutil;

import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class TarUtil {

  public File unTar(File tarFile) throws IOException {
    return unTar(Files.newInputStream(tarFile.toPath()));
  }

  public File unTar(InputStream tar) throws IOException {
    File destDir = Files.createTempDirectory("de-gesellix-tests").toFile();
    destDir.deleteOnExit();

    TarArchiveInputStream tis = new TarArchiveInputStream(tar);
    TarArchiveEntry tarEntry;
    while ((tarEntry = tis.getNextTarEntry()) != null) {
      File outputFile = new File(destDir, tarEntry.getName());
      if (tarEntry.isDirectory()) {
        if (!outputFile.exists()) {
          outputFile.mkdirs();
        }
      }
      else {
        outputFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(outputFile);
        BufferedSink sink = Okio.buffer(Okio.sink(fos));
        sink.writeAll(Okio.buffer(Okio.source(tis)));
        sink.flush();
        sink.close();
        fos.close();
      }
    }
    tis.close();
    return destDir;
  }

  public InputStream tar(File file) throws IOException {
    File destDir = Files.createTempDirectory("de-gesellix-tests").toFile();
    destDir.deleteOnExit();

    File tmpFile = new File(destDir, file.getName() + ".tar");
    tmpFile.deleteOnExit();

    TarArchiveOutputStream tos = new TarArchiveOutputStream(Files.newOutputStream(tmpFile.toPath()));
    TarArchiveEntry archiveEntry = tos.createArchiveEntry(file, file.getName());
    tos.putArchiveEntry(archiveEntry);
    Sink sink = Okio.sink(tos);
    Okio.buffer(Okio.source(file)).readAll(sink);
    sink.flush();
    tos.closeArchiveEntry();
    tos.flush();
    tos.finish();
    tos.close();

    return Files.newInputStream(tmpFile.toPath());
  }
}
