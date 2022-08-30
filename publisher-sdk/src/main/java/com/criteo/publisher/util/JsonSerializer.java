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

package com.criteo.publisher.util;

import androidx.annotation.NonNull;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import okio.BufferedSink;
import okio.Okio;

public class JsonSerializer {

  @NonNull
  private final Moshi moshi;

  public JsonSerializer(@NonNull Moshi moshi) {
    this.moshi = moshi;
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
  public <T> void write(
      @NonNull T object,
      @NonNull OutputStream outputStream
  ) throws IOException {
    try {
      BufferedSink out = Okio.buffer(Okio.sink(outputStream));
      JsonAdapter adapter;

      // At runtime we will have specific implementation of list (e.g. ArrayList)
      // Moshi does not support serialization of specific list subtypes out of the box
      // If we receive such instance just create JsonAdapter for List
      if (object instanceof List) {
        adapter = moshi.adapter(List.class);
      } else {
        adapter = moshi.adapter((Class<T>) object.getClass());
      }

      adapter.toJson(out, object);
      out.flush();
    } catch (JsonDataException e) {
      throw new IOException(e);
    }
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
    T object;
    try {
      object = moshi.adapter(expectedClass).fromJson(Okio.buffer(Okio.source(inputStream)));
    } catch (JsonDataException e) {
      throw new IOException(e);
    }

    if (object == null) {
      throw new EOFException();
    }

    return object;
  }
}
