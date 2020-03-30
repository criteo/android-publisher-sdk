package com.criteo.publisher.csm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.criteo.publisher.Util.PreconditionsUtil;
import com.squareup.tape.FileException;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.ObjectQueue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TapeMetricSendingQueue implements MetricSendingQueue {

  @NonNull
  private final ObjectQueue<Metric> queue;

  @VisibleForTesting
  TapeMetricSendingQueue(@NonNull ObjectQueue<Metric> queue) {
    this.queue = queue;
  }

  @NonNull
  @VisibleForTesting
  static ObjectQueue<Metric> createFileObjectQueue(
      @NonNull File file,
      @NonNull MetricParser parser
  ) throws IOException {
    return new FileObjectQueue<>(file, new MetricConverter(parser));
  }

  @Override
  public boolean offer(@NonNull Metric metric) {
    try {
      queue.add(metric);
      return true;
    } catch (FileException e) {
      PreconditionsUtil.throwOrLog(e);
      return false;
    }
  }

  @NonNull
  @Override
  public List<Metric> poll(int max) {
    List<Metric> metrics = new ArrayList<>();
    try {
      for (int i = 0; i < max; i++) {
        Metric metric = queue.peek();
        if (metric == null) {
          break;
        }

        metrics.add(metric);
        queue.remove();
      }
      return metrics;
    } catch (FileException e) {
      PreconditionsUtil.throwOrLog(e);
      return metrics;
    }
  }

  static class MetricConverter implements FileObjectQueue.Converter<Metric> {

    @NonNull
    private final MetricParser parser;

    MetricConverter(@NonNull MetricParser parser) {
      this.parser = parser;
    }

    @Nullable
    @Override
    public Metric from(@Nullable byte[] bytes) throws IOException {
      if (bytes == null) {
        return null;
      }

      try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
        return parser.read(input);
      }
    }

    @Override
    public void toStream(@Nullable Metric metric, @Nullable OutputStream outputStream) throws IOException {
      if (metric != null && outputStream != null) {
        parser.write(metric, outputStream);
      }
    }
  }
}
