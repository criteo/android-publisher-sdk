package com.criteo.publisher.csm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.AtomicFile;
import com.criteo.publisher.BuildConfig;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

class MetricDirectory {

  private static final String METRIC_FILE_EXTENSION = ".csm";

  @NonNull
  private final Context context;

  @NonNull
  private final MetricParser parser;

  MetricDirectory(
      @NonNull Context context,
      @NonNull MetricParser parser
  ) {
    this.context = context;
    this.parser = parser;
  }

  Collection<File> listFiles() {
    File[] files = getDirectoryFile().listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(METRIC_FILE_EXTENSION);
      }
    });

    if (files == null) {
      return Collections.emptyList();
    }

    return Arrays.asList(files);
  }

  @NonNull
  File createMetricFile(@NonNull String impressionId) {
    String metricFilename = impressionId + METRIC_FILE_EXTENSION;
    return new File(getDirectoryFile(), metricFilename);
  }

  @NonNull
  SyncMetricFile createSyncMetricFile(@NonNull File metricFile) {
    AtomicFile atomicFile = new AtomicFile(metricFile);
    return new SyncMetricFile(atomicFile, parser);
  }

  @VisibleForTesting
  @NonNull
  File getDirectoryFile() {
    return context.getDir(BuildConfig.CSM_DIRECTORY, Context.MODE_PRIVATE);
  }

}
