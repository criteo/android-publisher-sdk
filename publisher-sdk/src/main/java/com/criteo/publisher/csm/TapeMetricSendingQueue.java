package com.criteo.publisher.csm;

import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.util.PreconditionsUtil;
import com.squareup.tape.FileException;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.ObjectQueue;
import com.squareup.tape.QueueFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class TapeMetricSendingQueue extends MetricSendingQueue {

  private final Object queueLock = new Object();

  @Nullable
  @GuardedBy("queueLock")
  private ObjectQueue<Metric> queue;

  @Nullable
  private Method usedBytesMethod;

  @Nullable
  private QueueFile queueFile;

  @NonNull
  private final MetricObjectQueueFactory queueFactory;

  TapeMetricSendingQueue(@NonNull MetricObjectQueueFactory queueFactory) {
    this.queueFactory = queueFactory;
    this.usedBytesMethod = null;
    this.queueFile = null;
  }

  @Override
  boolean offer(@NonNull Metric metric) {
    synchronized (queueLock) {
      createQueueIfNecessary();

      try {
        queue.add(metric);
        return true;
      } catch (FileException e) {
        PreconditionsUtil.throwOrLog(e);
        return false;
      }
    }
  }

  @NonNull
  @Override
  List<Metric> poll(int max) {
    synchronized (queueLock) {
      createQueueIfNecessary();

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
  }

  @Override
  int getTotalSize() {
    synchronized (queueLock) {
      // This size is mainly used to bound this queue. For this, it is not enough to get the size of
      // the queue file. The file grows in power of 2. Even if it is the real size, then this means
      // that if the limit is just after a power of 2, then we will have a lot of available space,
      // but we would not use it. If the limit is just before a power of 2, then we will lose half
      // the capacity of the queue.
      // Moreover the file is shrinked after some removal. This is a detail of the implementation.
      // And even if we make some room in the queue, we will not see it.
      // There is a usedBytes method in the internal queueFile of the file object queue. This method
      // is used to get the real size of the queue.

      createQueueIfNecessary();

      if (!(queue instanceof FileObjectQueue)) {
        return 0;
      }

      try {
        Method usedBytesMethod = getUsedBytesMethod();
        QueueFile queueFile = getQueueFile((FileObjectQueue) queue);
        return (Integer) usedBytesMethod.invoke(queueFile);
      } catch (Exception e) {
        PreconditionsUtil.throwOrLog(e);
        return 0;
      }
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
  private QueueFile getQueueFile(@NonNull FileObjectQueue fileObjectQueue) throws ReflectiveOperationException, ClassCastException {
      if (queueFile == null) {
        Field queueFileField = FileObjectQueue.class.getDeclaredField("queueFile");
        queueFileField.setAccessible(true);
        queueFile = (QueueFile) queueFileField.get(fileObjectQueue);
      }

      return queueFile;
  }

  private void createQueueIfNecessary() {
    if (queue == null) {
      queue = queueFactory.create();
    }
  }
}
