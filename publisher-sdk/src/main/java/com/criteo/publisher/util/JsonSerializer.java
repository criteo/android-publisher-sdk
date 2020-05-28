package com.criteo.publisher.util;

import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

public class JsonSerializer {

  @NonNull
  private final Gson gson;

  public JsonSerializer(@NonNull Gson gson) {
    this.gson = gson;
  }

  /**
   * Write the given object in the given output
   * <p>
   * If any error occurs while writing, then an {@link IOException} is thrown.
   * <p>
   * The given output is not {@linkplain OutputStream#close() closed}. If it should be, then it is
   * the responsibility of the caller to do so.
   *
   * @param object object to write in output
   * @param outputStream output where to write in
   * @throws IOException if any error occurs
   */
  public void write(
      @NonNull Object object,
      @NonNull OutputStream outputStream
  ) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(outputStream);

    try {
      gson.toJson(object, writer);
    } catch (JsonIOException e) {
      throw new IOException(e);
    }

    writer.flush();
  }

  /**
   * Read an expected class object from the given input.
   * <p>
   * If any error occurs while reading, then an {@link IOException} is thrown.
   * <p>
   * The given input is not {@linkplain InputStream#close() closed}. If it should be, then it is the
   * responsibility of the caller to do so.
   *
   * @param expectedClass type of the object to read
   * @param inputStream input where to read from
   * @return read object from the stream
   * @throws IOException if any error occurs
   */
  @NonNull
  public <T> T read(
      @NonNull Class<T> expectedClass,
      @NonNull InputStream inputStream
  ) throws IOException {
    Reader reader = new InputStreamReader(inputStream);

    T object;
    try {
      object = gson.fromJson(reader, expectedClass);
    } catch (JsonParseException e) {
      throw new IOException(e);
    }

    if (object == null) {
      throw new EOFException();
    }

    return object;
  }
}
