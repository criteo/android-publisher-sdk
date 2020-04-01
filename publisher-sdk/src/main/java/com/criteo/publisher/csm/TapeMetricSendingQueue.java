package com.criteo.publisher.csm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.Util.PreconditionsUtil;
import com.squareup.tape.FileException;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.ObjectQueue;
import com.squareup.tape.QueueFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class TapeMetricSendingQueue extends MetricSendingQueue {

  @NonNull
  private final ObjectQueue<Metric> queue;

  @Nullable
  private Method usedBytesMethod;

  @Nullable
  private QueueFile queueFile;

  TapeMetricSendingQueue(@NonNull ObjectQueue<Metric> queue) {
    this.queue = queue;
    this.usedBytesMethod = null;
    this.queueFile = null;
  }

  @NonNull
  static ObjectQueue<Metric> createFileObjectQueue(
      @NonNull File file,
      @NonNull MetricParser parser
  ) throws IOException {
    return new FileObjectQueue<>(file, new MetricConverter(parser));
  }

  @Override
  boolean offer(@NonNull Metric metric) {
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
  List<Metric> poll(int max) {
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

  @Override
  int getTotalSize() {
    // This size is mainly used to bound this queue. For this, it is not enough to get the size of
    // the queue file. The file grows in power of 2. Even if it is the real size, then this means
    // that if the limit is just after a power of 2, then we will have a lot of available space, but
    // we would not use it. If the limit is just before a power of 2, then we will lose half the
    // capacity of the queue.
    // Moreover the file is shrinked after some removal. This is a detail of the implementation. And
    // even if we make some room in the queue, we will not see it.
    // There is a usedBytes method in the internal queueFile of the file object queue. This method
    // is used to get the real size of the queue.

    if (!(queue instanceof FileObjectQueue)) {
      return 0;
    }

    try {
      Method usedBytesMethod = getUsedBytesMethod();
      QueueFile queueFile = getQueueFile();
      return (Integer) usedBytesMethod.invoke(queueFile);
    } catch (Exception e) {
      PreconditionsUtil.throwOrLog(e);
      return 0;
    }
  }

  @NonNull
  private Method getUsedBytesMethod() throws ReflectiveOperationException {
    if (usedBytesMethod == null) {
      usedBytesMethod = QueueFile.class.getDeclaredMethod("usedBytes");
      usedBytesMethod.setAccessible(true);
    }

    return usedBytesMethod;
  }

  @NonNull
  private QueueFile getQueueFile() throws ReflectiveOperationException, ClassCastException {
    if (queueFile == null) {
      Field queueFileField = FileObjectQueue.class.getDeclaredField("queueFile");
      queueFileField.setAccessible(true);
      queueFile = (QueueFile) queueFileField.get(queue);
    }

    return queueFile;
  }

  private static class MetricConverter implements FileObjectQueue.Converter<Metric> {

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
