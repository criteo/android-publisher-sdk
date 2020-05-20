package com.criteo.publisher.csm;

import static org.junit.Assert.assertNotNull;

import java.io.File;

public class MetricDirectoryHelper {

  static void clear(MetricDirectory directory) {
    File repositoryDirectory = directory.getDirectoryFile();
    File[] files = repositoryDirectory.listFiles();

    assertNotNull(files);

    for (File file : files) {
      file.delete();
    }
    repositoryDirectory.delete();
  }

}
