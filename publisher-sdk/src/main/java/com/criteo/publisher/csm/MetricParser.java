package com.criteo.publisher.csm;

import android.support.annotation.NonNull;
import com.criteo.publisher.util.JsonSerializer;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MetricParser {

  @NonNull
  private final Gson gson;

  @NonNull
  private final JsonSerializer serializer;

  public MetricParser(@NonNull Gson gson, @NonNull JsonSerializer serializer) {
    this.gson = gson;
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
    Metric metric;
    try {
      metric = gson.fromJson(new InputStreamReader(inputStream), Metric.class);
    } catch (JsonParseException e) {
      throw new IOException(e);
    }

    if (metric == null) {
      throw new EOFException();
    }
    return metric;
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
