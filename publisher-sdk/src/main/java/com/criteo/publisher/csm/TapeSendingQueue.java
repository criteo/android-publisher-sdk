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

import static com.criteo.publisher.csm.SendingQueueLogMessage.onErrorWhenPollingQueueFile;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.util.PreconditionsUtil;
import com.squareup.tape.FileException;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.ObjectQueue;
import com.squareup.tape.QueueFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class TapeSendingQueue<T> implements ConcurrentSendingQueue<T> {

  @NonNull
  private final Logger logger = LoggerFactory.getLogger(TapeSendingQueue.class);

  @NonNull
  private final Object queueLock = new Object();

  @Nullable
  @GuardedBy("queueLock")
  private ObjectQueue<T> queue;

  @Nullable
  private Method usedBytesMethod;

  @Nullable
  private QueueFile queueFile;

  @NonNull
  private final ObjectQueueFactory<T> queueFactory;

  TapeSendingQueue(@NonNull ObjectQueueFactory<T> queueFactory) {
    this.queueFactory = queueFactory;
    this.usedBytesMethod = null;
    this.queueFile = null;
  }

  @Override
  public boolean offer(@NonNull T element) {
    synchronized (queueLock) {
      ObjectQueue<T> queue = createQueueIfNecessary();

      try {
        queue.add(element);
        return true;
      } catch (FileException e) {
        PreconditionsUtil.throwOrLog(e);
        return false;
      }
    }
  }

  @NonNull
  @Override
  public List<T> poll(int max) {
    synchronized (queueLock) {
      ObjectQueue<T> queue = createQueueIfNecessary();

      List<T> elements = new ArrayList<>();
      Exception exception = null;

      for (int i = 0; i < max; i++) {
        try {
          T element = queue.peek();

          if (element == null) {
            break;
          }

          elements.add(element);
        } catch (FileException e) {
          if (exception == null) {
            exception = e;
          } else {
            exception.addSuppressed(e);
          }
        } finally {
          try {
            // There is a bug in tape queue implementation making byte array full of 0 written. To
            // recover and not block the queue at this point, we always remove the first element
            // even in case of error.
            // It is not possible to detect this in the offer method, because elements are added at
            // the end of the queue and we can only peek at the beginning of it.
            if (queue.size() > 0) {
              queue.remove();
            }
          } catch (FileException e) {
            if (exception == null) {
              exception = e;
            } else {
              exception.addSuppressed(e);
            }
          }
        }
      }

      if (exception != null) {
        logger.log(onErrorWhenPollingQueueFile(exception));
      }

      return elements;
    }
  }

  @Override
  public int getTotalSize() {
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

      ObjectQueue<T> queue = createQueueIfNecessary();

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
  @VisibleForTesting
  QueueFile getQueueFile(@NonNull FileObjectQueue<?> fileObjectQueue)
      throws ReflectiveOperationException, ClassCastException {
    if (queueFile == null) {
      Field queueFileField = FileObjectQueue.class.getDeclaredField("queueFile");
      queueFileField.setAccessible(true);
      queueFile = (QueueFile) queueFileField.get(fileObjectQueue);
    }

    return queueFile;
  }

  private ObjectQueue<T> createQueueIfNecessary() {
    if (queue == null) {
      queue = queueFactory.create();
    }
    return queue;
  }
}
