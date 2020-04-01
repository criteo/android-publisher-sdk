package com.criteo.publisher.csm;

import android.support.annotation.NonNull;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

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
   * At most, one {@link SyncMetricFile} should exist for an underlying file. This method
   * ensure this with a logic similar to {@link ConcurrentMap#computeIfAbsent(Object, Function)}.
   * Note that this method is not directly used because it requires Android API level >= 24.
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
