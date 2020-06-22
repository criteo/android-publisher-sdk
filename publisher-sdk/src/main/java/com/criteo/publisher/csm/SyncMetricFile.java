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
import android.util.AtomicFile;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.csm.MetricRepository.MetricUpdater;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * The {@link SyncMetricFile} internally uses {@link AtomicFile} to ensure atomicity of any write
 * even in case of unexpected application shutdown. Although, atomic files should not be accessed
 * by multiple threads at the same time. The {@link SyncMetricFile} handle this synchronization
 * level per file.
 * <p>
 * So only, at most, one {@link SyncMetricFile} should exist for an underlying file.
 */
@RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR1)
class SyncMetricFile {

  @NonNull
  private final String impressionId;

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

  @NonNull
  private volatile SoftReference<Metric> metricInMemory;

  SyncMetricFile(
      @NonNull String impressionId,
      @NonNull AtomicFile file,
      @NonNull MetricParser parser
  ) {
    this.impressionId = impressionId;
    this.file = file;
    this.parser = parser;
    this.metricInMemory = new SoftReference<>(null);
  }

  Metric read() throws IOException {
    synchronized (fileLock) {
      Metric inMemory = metricInMemory.get();
      if (inMemory != null) {
        return inMemory;
      }

      Metric inFile = readFromFile();
      metricInMemory = new SoftReference<>(inFile);
      return inFile;
    }
  }

  @VisibleForTesting
  void write(Metric metric) throws IOException {
    synchronized (fileLock) {
      // Invalidate in-memory version. This is to prevent any inconsistency in case of IO error.
      metricInMemory = new SoftReference<>(null);

      writeInFile(metric);
      metricInMemory = new SoftReference<>(metric);
    }
  }

  void update(MetricUpdater updater) throws IOException {
    synchronized (fileLock) {
      Metric metric = read();

      Metric.Builder builder = metric.toBuilder();
      updater.update(builder);
      Metric newMetric = builder.build();

      write(newMetric);
    }
  }

  /**
   * Move the metric represented by this instance with the given move definition.
   * <p>
   * The metric file is read, deleted and moved into the destination. If the move is a success, then
   * the file of this metric is kept deleted. If the move is not a success because the destination
   * rejected it or there was an error, then it is rollback, and the file is rewritten on disk. If
   * there is an error during rollback or a crash just before it, then this data is lost.
   * <p>
   * If done the other way (deleting after inserting into destination), then there would be a
   * potential risk of duplication of this metric file. In the context of metrics, it is preferable
   * to lose some data rather than producing duplicate ones.
   *
   * @param mover definition of the move to do
   * @throws IOException in case of error during read or rollback
   */
  void moveWith(MetricMover mover) throws IOException {
    synchronized (fileLock) {
      Metric metric = read();

      delete();
      boolean success = false;
      try {
        if (mover.offerToDestination(metric)) {
          success = true;
        }
      } finally {
        if (!success) {
          write(metric);
        }
      }
    }
  }

  @VisibleForTesting
  void delete() {
    synchronized (fileLock) {
      metricInMemory = new SoftReference<>(null);
      file.delete();
    }
  }

  @NonNull
  private Metric readFromFile() throws IOException {
    if (!file.getBaseFile().exists()) {
      return Metric.builder(impressionId).build();
    }

    try (InputStream is = file.openRead();
        BufferedInputStream bis = new BufferedInputStream(is)) {
      return parser.read(bis);
    }
  }

  private void writeInFile(@NonNull Metric metric) throws IOException {
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
