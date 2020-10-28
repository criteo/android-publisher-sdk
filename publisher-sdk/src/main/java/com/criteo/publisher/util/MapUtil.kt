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

package com.criteo.publisher.util

import java.util.concurrent.ConcurrentMap

/**
 * If the specified key is not already associated with a value (or is mapped to {@code null}),
 * attempts to compute its value using the given mapping function and enters it into this map.
 *
 * If the mapping function itself throws an (unchecked) exception, the exception is rethrown, and
 * no mapping is recorded.
 *
 * Unlike [ConcurrentMap.computeIfAbsent], the mapping function have the right to update the map
 * itself during the computation. If the mapping for the given [key] is set during execution of
 * the mapping function (either by the mapping function itself or by another thread), then the set
 * mapping is kept and returned and the computed value is thrown away.
 *
 * @see ConcurrentMap.computeIfAbsent
 */
inline fun <K, V> ConcurrentMap<K, V>.getOrCompute(key: K, defaultValue: () -> V): V {
  val value = get(key)
  return if (value == null) {
    val newValue = defaultValue()

    // A new concurrent value might have been put during creation of the new value.
    // It would be possible to avoid concurrency by synchronizing the get+put but then the mapping
    // function could not be able to update the map itself.
    val existingValue = putIfAbsent(key, newValue)

    existingValue ?: newValue
  } else {
    value
  }
}

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K, V?>.filterNotNullValues() = filterValues { it != null } as Map<K, V>
