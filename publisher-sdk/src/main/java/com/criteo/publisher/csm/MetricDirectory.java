package com.criteo.publisher.csm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.AtomicFile;
import com.criteo.publisher.BuildConfig;
import com.criteo.publisher.Util.PreconditionsUtil;
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
    File[] files;

    try {
      files = getDirectoryFile().listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(METRIC_FILE_EXTENSION);
        }
      });
    } catch (SecurityException e) {
      PreconditionsUtil.throwOrLog(e);
      return Collections.emptyList();
    }

    if (files == null) {
      return Collections.emptyList();
    }

    return Arrays.asList(files);
  }

  @NonNull
  File createMetricFile(@NonNull String impressionId) {
    String metricFilename = getMetricFilenameFromImpressionId(impressionId);
    return new File(getDirectoryFile(), metricFilename);
  }

  @NonNull
  SyncMetricFile createSyncMetricFile(@NonNull File metricFile) {
    String impressionId = getImpressionIdFromMetricFilename(metricFile);
    AtomicFile atomicFile = new AtomicFile(metricFile);
    return new SyncMetricFile(impressionId, atomicFile, parser);
  }

  @VisibleForTesting
  @NonNull
  File getDirectoryFile() {
    return context.getDir(BuildConfig.csmDirectory, Context.MODE_PRIVATE);
  }

  /**
   * Produce a metric filename from the given impression id.
   *
   * @param impressionId impression id to transform
   * @return filename for that impression id
   * @see #getImpressionIdFromMetricFilename(File)
   */
  @NonNull
  private String getMetricFilenameFromImpressionId(@NonNull String impressionId) {
    return impressionId + METRIC_FILE_EXTENSION;
  }

  /**
   * Produce an impression id from the given metric filename.
   * <p>
   * This is the dual of {@link #getMetricFilenameFromImpressionId(String)}: The impression ID is a
   * string, and the metric filename is the impression ID with an extension. To find again the
   * impression ID from the filename, then this methods only have to remove the extension.
   *
   * @param metricFile file representing the metric
   * @return impression ID of the given file
   */
  @NonNull
  private String getImpressionIdFromMetricFilename(@NonNull File metricFile) {
    String metricFilename = metricFile.getName();
    return metricFilename.substring(0, metricFilename.length() - METRIC_FILE_EXTENSION.length());
  }

}
