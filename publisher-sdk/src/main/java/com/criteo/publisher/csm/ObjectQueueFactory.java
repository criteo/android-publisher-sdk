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

import static com.criteo.publisher.csm.SendingQueueLogMessage.onRecoveringFromStaleQueueFile;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.util.JsonSerializer;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.InMemoryObjectQueue;
import com.squareup.tape.ObjectQueue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class ObjectQueueFactory<T> {

  @NonNull
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @NonNull
  private final Context context;

  @NonNull
  private final JsonSerializer jsonSerializer;

  @NonNull
  private final SendingQueueConfiguration<T> sendingQueueConfiguration;

  public ObjectQueueFactory(
      @NonNull Context context,
      @NonNull JsonSerializer jsonSerializer,
      @NonNull SendingQueueConfiguration<T> sendingQueueConfiguration
  ) {
    this.context = context;
    this.jsonSerializer = jsonSerializer;
    this.sendingQueueConfiguration = sendingQueueConfiguration;
  }

  @NonNull
  public ObjectQueue<T> create() {
    File file = getQueueFile();
    return createTapeObjectQueue(file);
  }

  @VisibleForTesting
  File getQueueFile() {
    return new File(context.getFilesDir(), sendingQueueConfiguration.getQueueFilename());
  }

  private ObjectQueue<T> createTapeObjectQueue(@NonNull File file) {
    Exception exception;
    try {
      FileObjectQueue<T> queue = new FileObjectQueue<>(file, new AdapterConverter<>(
          jsonSerializer,
          sendingQueueConfiguration.getElementClass()
      ));

      // Try to peek to be sure that the queue is not corrupted.
      queue.peek();

      return queue;
    } catch (Exception e) {
      exception = e;
    }

    // Maybe the file is stale: we're inside the publisher's app directory. So we're not protected
    // against any modifications. To handle this, we can retry with a new file.
    boolean isDeleted = delete(file);

    if (isDeleted) {
      try {
        return new FileObjectQueue<>(file, new AdapterConverter<>(
            jsonSerializer,
            sendingQueueConfiguration.getElementClass()
        ));
      } catch (IOException e) {
        exception.addSuppressed(e);
      } finally {
        logger.log(onRecoveringFromStaleQueueFile(exception));
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

  @VisibleForTesting
  static class AdapterConverter<T> implements FileObjectQueue.Converter<T> {

    @NonNull
    private final JsonSerializer jsonSerializer;

    @NonNull
    private final Class<T> elementClass;

    AdapterConverter(
        @NonNull JsonSerializer jsonSerializer,
        @NonNull Class<T> elementClass
    ) {
      this.jsonSerializer = jsonSerializer;
      this.elementClass = elementClass;
    }

    @Nullable
    @Override
    public T from(@Nullable byte[] bytes) throws IOException {
      if (bytes == null) {
        return null;
      }

      try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
        return jsonSerializer.read(elementClass, input);
      }
    }

    @Override
    public void toStream(@Nullable T element, @Nullable OutputStream outputStream) throws IOException {
      if (element != null && outputStream != null) {
        jsonSerializer.write(element, outputStream);
      }
    }
  }
}
