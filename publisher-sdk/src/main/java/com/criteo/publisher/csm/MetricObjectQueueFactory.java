package com.criteo.publisher.csm;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.PreconditionsUtil;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.InMemoryObjectQueue;
import com.squareup.tape.ObjectQueue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class MetricObjectQueueFactory {

  @NonNull
  private final Context context;

  @NonNull
  private final MetricParser metricParser;

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  public MetricObjectQueueFactory(
      @NonNull Context context,
      @NonNull MetricParser metricParser,
      @NonNull BuildConfigWrapper buildConfigWrapper
  ) {
    this.context = context;
    this.metricParser = metricParser;
    this.buildConfigWrapper = buildConfigWrapper;
  }

  @NonNull
  public ObjectQueue<Metric> create() {
    File file = getQueueFile();
    ObjectQueue<Metric> tapeObjectQueue = createTapeObjectQueue(file);
    return tapeObjectQueue;
  }

  @VisibleForTesting
  File getQueueFile() {
    return new File(context.getFilesDir(), buildConfigWrapper.getCsmQueueFilename());
  }

  private ObjectQueue<Metric> createTapeObjectQueue(@NonNull File file) {
    Exception exception;
    try {
      return new FileObjectQueue<>(file, new MetricConverter(metricParser));
    } catch (IOException e) {
      exception = e;
    }

    // Maybe the file is stale: we're inside the publisher's app directory. So we're not protected
    // against any modifications. To handle this, we can retry with a new file.
    boolean isDeleted = delete(file);

    if (isDeleted) {
      try {
        return new FileObjectQueue<>(file, new MetricConverter(metricParser));
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

  @VisibleForTesting
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
