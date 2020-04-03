package com.criteo.publisher.csm;

import static com.criteo.publisher.csm.TapeMetricSendingQueue.createFileObjectQueue;

import android.content.Context;
import android.support.annotation.NonNull;
import com.criteo.publisher.DependencyProvider.Factory;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.PreconditionsUtil;
import com.squareup.tape.InMemoryObjectQueue;
import com.squareup.tape.ObjectQueue;
import java.io.File;
import java.io.IOException;

public class MetricSendingQueueFactory implements Factory<MetricSendingQueue> {

  @NonNull
  private final Context context;

  @NonNull
  private final MetricParser metricParser;

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  public MetricSendingQueueFactory(
      @NonNull Context context,
      @NonNull MetricParser metricParser,
      @NonNull BuildConfigWrapper buildConfigWrapper
  ) {
    this.context = context;
    this.metricParser = metricParser;
    this.buildConfigWrapper = buildConfigWrapper;
  }

  @NonNull
  @Override
  public MetricSendingQueue create() {
    File file = new File(context.getFilesDir(), buildConfigWrapper.getCsmQueueFilename());
    ObjectQueue<Metric> tapeObjectQueue = createTapeObjectQueue(file);
    MetricSendingQueue tapeQueue = new TapeMetricSendingQueue(tapeObjectQueue);
    return new BoundedMetricSendingQueue(tapeQueue, buildConfigWrapper);
  }

  private ObjectQueue<Metric> createTapeObjectQueue(@NonNull File file) {
    Exception exception;
    try {
      return createFileObjectQueue(file, metricParser);
    } catch (IOException e) {
      exception = e;
    }

    // Maybe the file is stale: we're inside the publisher's app directory. So we're not protected
    // against any modifications. To handle this, we can retry with a new file.
    boolean isDeleted = delete(file);

    if (isDeleted) {
      try {
        return createFileObjectQueue(file, metricParser);
      } catch (IOException e) {
        exception.addSuppressed(e);
      } finally {
        PreconditionsUtil.throwOrLog(exception);
      }
    }

    // If this still does not work, we fallback on an in-memory solution.
    return new InMemoryObjectQueue<>();
  }

  private boolean delete(@NonNull File file) {
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File childFile : files) {
          if (!delete(childFile)) {
            return false;
          }
        }
      }
    }
    return file.delete();
  }
}
