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

import android.os.Build.VERSION_CODES;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR1)
class FileMetricRepository extends MetricRepository {

  private static final String TAG = FileMetricRepository.class.getSimpleName();

  @NonNull
  private final MetricDirectory directory;

  @NonNull
  private final ConcurrentMap<File, SyncMetricFile> metricFileById = new ConcurrentHashMap<>();

  FileMetricRepository(@NonNull MetricDirectory directory) {
    this.directory = directory;
  }

  @Override
  void addOrUpdateById(@NonNull String impressionId, @NonNull MetricUpdater updater) {
    File metricFile = directory.createMetricFile(impressionId);
    SyncMetricFile syncMetricFile = getOrCreateMetricFile(metricFile);

    try {
      syncMetricFile.update(updater);
    } catch (IOException e) {
      Log.d(TAG, "Error while updating metric", e);
    }
  }

  @Override
  void moveById(@NonNull String impressionId, @NonNull MetricMover mover) {
    File metricFile = directory.createMetricFile(impressionId);
    SyncMetricFile syncMetricFile = getOrCreateMetricFile(metricFile);

    try {
      syncMetricFile.moveWith(mover);
    } catch (IOException e) {
      Log.d(TAG, "Error while moving metric", e);
    }
  }

  @Override
  @NonNull
  Collection<Metric> getAllStoredMetrics() {
    Collection<File> files = directory.listFiles();

    List<Metric> metrics = new ArrayList<>(files.size());
    for (File metricFile : files) {
      try {
        Metric metric = getOrCreateMetricFile(metricFile).read();
        metrics.add(metric);
      } catch (IOException e) {
        Log.d(TAG, "Error while reading metric", e);
      }
    }
    return metrics;
  }

  @Override
  int getTotalSize() {
    int size = 0;
    Collection<File> files = directory.listFiles();
    for (File file : files) {
      size += file.length();
    }
    return size;
  }

  @Override
  boolean contains(@NonNull String impressionId) {
    File metricFile = directory.createMetricFile(impressionId);
    return directory.listFiles().contains(metricFile);
  }

  /**
   * Atomically get or create a synchronized metric file on the given file.
   * <p>
   * At most, one {@link SyncMetricFile} should exist for an underlying file. This method ensure
   * this with a logic similar to {@link ConcurrentMap#computeIfAbsent(Object, Function)}. Note that
   * this method is not directly used because it requires Android API level >= 24.
   *
   * @param metricFile underlying file to synchronized
   * @return unique instance of synchronized and atomic file over given one
   */
  @NonNull
  private SyncMetricFile getOrCreateMetricFile(@NonNull File metricFile) {
    SyncMetricFile oldMetric = metricFileById.get(metricFile);
    if (oldMetric == null) {
      SyncMetricFile newMetric = directory.createSyncMetricFile(metricFile);
      oldMetric = (oldMetric = metricFileById.putIfAbsent(metricFile, newMetric)) == null
          ? newMetric : oldMetric;
    }
    return oldMetric;
  }

}
