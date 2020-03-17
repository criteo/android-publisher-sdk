package com.criteo.publisher.csm;

import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.v4.util.AtomicFile;
import com.criteo.publisher.csm.MetricRepository.MetricUpdater;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The {@link SyncMetricFile} internally uses {@link AtomicFile} to ensure atomicity of any write
 * even in case of unexpected application shutdown. Although, atomic files should not be accessed
 * by multiple threads at the same time. The {@link SyncMetricFile} handle this synchronization
 * level per file.
 * <p>
 * So only, at most, one {@link SyncMetricFile} should exist for an underlying file.
 */
class SyncMetricFile {

  /**
   * This file is protected at runtime by {@link #fileLock}. Although, there is no locking
   * mechanism between different VMs (so different apps). It is expected that the given file is in
   * the application storage, so locking between apps is not required.
   */
  @GuardedBy("fileLock")
  @NonNull
  private final AtomicFile file;

  @NonNull
  private final Object fileLock = new Object();

  @NonNull
  private final MetricParser parser;

  SyncMetricFile(
      @NonNull AtomicFile file,
      @NonNull MetricParser parser
  ) {
    this.file = file;
    this.parser = parser;
  }

  Metric read() throws IOException {
    synchronized (fileLock) {
      return readMetric();
    }
  }

  void update(MetricUpdater updater) throws IOException {
    synchronized (fileLock) {
      Metric metric = readMetric();

      Metric.Builder builder = metric.toBuilder();
      updater.update(builder);
      Metric newMetric = builder.build();

      writeMetric(newMetric);
    }
  }

  @NonNull
  private Metric readMetric() throws IOException {
    if (!file.getBaseFile().exists()) {
      return Metric.builder().build();
    }

    try (InputStream is = file.openRead();
        BufferedInputStream bis = new BufferedInputStream(is)) {
      return parser.read(bis);
    }
  }

  private void writeMetric(@NonNull Metric metric) throws IOException {
    try (FileOutputStream fos = file.startWrite();
        BufferedOutputStream bos = new BufferedOutputStream(fos)) {
      try {
        parser.write(metric, bos);
        file.finishWrite(fos);
      } catch (IOException e) {
        file.failWrite(fos);
        throw e;
      }
    }
  }

}
