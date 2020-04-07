package com.criteo.publisher.csm;

import android.support.annotation.NonNull;
import com.criteo.publisher.util.JsonSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MetricParser {

  @NonNull
  private final JsonSerializer serializer;

  public MetricParser(@NonNull JsonSerializer serializer) {
    this.serializer = serializer;
  }

  /**
   * Read a metric from the given input stream
   * <p>
   * If the given input is empty, or does not represent readable content, then an {@link
   * IOException} is thrown.
   * <p>
   * The given input is never {@linkplain InputStream#close() closed}. If it should be, then it is
   * the responsibility of the caller to do so.
   *
   * @param inputStream input where to read from
   * @return read metric object
   * @throws IOException if metric object could not be successfully read
   */
  @NonNull
  Metric read(@NonNull InputStream inputStream) throws IOException {
    return serializer.read(Metric.class, inputStream);
  }

  /**
   * Write the given metric object in the given output
   * <p>
   * If any error occurs while writing, then an {@link IOException} is thrown.
   * <p>
   * The given output is not {@linkplain OutputStream#close() closed}. If it should be, then it is
   * the responsibility of the caller to do so.
   *
   * @param metric metric to write in output
   * @param outputStream output where to write in
   * @throws IOException if any error occurs
   */
  void write(@NonNull Metric metric, @NonNull OutputStream outputStream) throws IOException {
    serializer.write(metric, outputStream);
  }

}
