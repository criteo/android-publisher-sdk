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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class StreamUtil {

  private StreamUtil() {

  }

  @NonNull
  public static String readStream(InputStream in) throws IOException {
    StringBuilder response = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
    }
    return response.toString();
  }

}
