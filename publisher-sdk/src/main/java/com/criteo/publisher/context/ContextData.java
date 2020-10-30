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

package com.criteo.publisher.context;

import static com.criteo.publisher.annotation.Incubating.CONTEXT;
import static java.util.Collections.unmodifiableList;

import androidx.annotation.NonNull;
import com.criteo.publisher.annotation.Incubating;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextData {

  /**
   * <h1>Definition</h1>
   * A single URL of the content, for buy-side contextualization or review.
   * <p>
   * <h1>Type</h1>
   * String, like <em>https://www.criteo.com</em>
   * <p>
   * See <a href="https://github.com/InteractiveAdvertisingBureau/AdCOM/blob/master/AdCOM%20v1.0%20FINAL.md#object--content-">Object:
   * Content</a>
   */
  public static final String CONTENT_URL = "content.url";

  private final Map<String, Object> data = new HashMap<>();

  /**
   * Set a new context value in this object
   *
   * @param key key-path of an OpenRTB field, or of a Criteo field. Refer to constants in {@link ContextData}
   * @param value new value for the given key
   * @return <code>this</code> to chain calls
   */
  @Incubating(CONTEXT)
  public ContextData set(@NonNull String key, @NonNull String value) {
    data.put(key, value);
    return this;
  }

  /**
   * Set a new context value in this object
   *
   * @param key key-path of an OpenRTB field, or of a Criteo field. Refer to constants in {@link ContextData}
   * @param value new value for the given key
   * @return <code>this</code> to chain calls
   */
  @Incubating(CONTEXT)
  public ContextData set(@NonNull String key, @NonNull Iterable<String> value) {
    // Copy data so it becomes constant
    List<String> list = new ArrayList<>();
    for (String v : value) {
      list.add(v);
    }

    data.put(key, unmodifiableList(list));
    return this;
  }

  /**
   * Set a new context value in this object
   *
   * @param key key-path of an OpenRTB field, or of a Criteo field. Refer to constants in {@link ContextData}
   * @param value new value for the given key
   * @return <code>this</code> to chain calls
   */
  @Incubating(CONTEXT)
  public ContextData set(@NonNull String key, long value) {
    data.put(key, value);
    return this;
  }

  /**
   * Set a new context value in this object
   *
   * @param key key-path of an OpenRTB field, or of a Criteo field. Refer to constants in {@link ContextData}
   * @param value new value for the given key
   * @return <code>this</code> to chain calls
   */
  @Incubating(CONTEXT)
  public ContextData set(@NonNull String key, double value) {
    data.put(key, value);
    return this;
  }

  Map<String, Object> getData() {
    return Collections.unmodifiableMap(data);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ContextData)) {
      return false;
    }
    ContextData that = (ContextData) o;
    return data.equals(that.data);
  }

  @Override
  public int hashCode() {
    return data.hashCode();
  }
}
