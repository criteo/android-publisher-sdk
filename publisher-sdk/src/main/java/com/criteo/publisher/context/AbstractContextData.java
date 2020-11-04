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

import static java.util.Collections.unmodifiableList;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractContextData<T extends AbstractContextData<T>> {

  @SuppressWarnings("unchecked")
  private final T myself = (T) this;

  @NonNull
  private final Map<String, Object> data = new LinkedHashMap<>();

  /**
   * Set a new context value in this object
   *
   * @param key key-path of an OpenRTB field, or of a Criteo field. Refer to constants in {@link ContextData}
   * @param value new value for the given key
   * @return <code>this</code> to chain calls
   */
  @Keep
  public T set(@NonNull String key, @NonNull String value) {
    data.put(key, value);
    return myself;
  }

  /**
   * Set a new context value in this object
   *
   * @param key key-path of an OpenRTB field, or of a Criteo field. Refer to constants in {@link ContextData}
   * @param value new value for the given key
   * @return <code>this</code> to chain calls
   */
  @Keep
  public T set(@NonNull String key, @NonNull Iterable<String> value) {
    // Copy data so it becomes constant
    List<String> list = new ArrayList<>();
    for (String v : value) {
      list.add(v);
    }

    data.put(key, unmodifiableList(list));
    return myself;
  }

  /**
   * Set a new context value in this object
   *
   * @param key key-path of an OpenRTB field, or of a Criteo field. Refer to constants in {@link ContextData}
   * @param value new value for the given key
   * @return <code>this</code> to chain calls
   */
  @Keep
  public T set(@NonNull String key, long value) {
    data.put(key, value);
    return myself;
  }

  /**
   * Set a new context value in this object
   *
   * @param key key-path of an OpenRTB field, or of a Criteo field. Refer to constants in {@link ContextData}
   * @param value new value for the given key
   * @return <code>this</code> to chain calls
   */
  @Keep
  public T set(@NonNull String key, double value) {
    data.put(key, value);
    return myself;
  }

  Map<String, Object> getData() {
    return Collections.unmodifiableMap(data);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractContextData)) {
      return false;
    }
    AbstractContextData<?> that = (ContextData) o;
    return data.equals(that.data);
  }

  @Override
  public int hashCode() {
    return data.hashCode();
  }
}
