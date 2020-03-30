package com.criteo.publisher.Util;

import android.support.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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
  public void write(@NonNull Object object, @NonNull OutputStream outputStream) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(outputStream);

    try {
      gson.toJson(object, writer);
    } catch (JsonIOException e) {
      throw new IOException(e);
    }

    writer.flush();
  }

}
