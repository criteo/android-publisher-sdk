/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.csm;

import android.content.Context;
import android.util.AtomicFile;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.JsonSerializer;
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
  private final BuildConfigWrapper buildConfigWrapper;

  @NonNull
  private final JsonSerializer jsonSerializer;

  MetricDirectory(
      @NonNull Context context,
      @NonNull BuildConfigWrapper buildConfigWrapper,
      @NonNull JsonSerializer jsonSerializer
  ) {
    this.context = context;
    this.buildConfigWrapper = buildConfigWrapper;
    this.jsonSerializer = jsonSerializer;
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
    String metricFilename = getMetricFilenameFromImpressionId(impressionId);
    return new File(getDirectoryFile(), metricFilename);
  }

  @NonNull
  SyncMetricFile createSyncMetricFile(@NonNull File metricFile) {
    String impressionId = getImpressionIdFromMetricFilename(metricFile);
    AtomicFile atomicFile = new AtomicFile(metricFile);
    return new SyncMetricFile(impressionId, atomicFile, jsonSerializer);
  }

  @VisibleForTesting
  @NonNull
  File getDirectoryFile() {
    return context.getDir(buildConfigWrapper.getCsmDirectoryName(), Context.MODE_PRIVATE);
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
