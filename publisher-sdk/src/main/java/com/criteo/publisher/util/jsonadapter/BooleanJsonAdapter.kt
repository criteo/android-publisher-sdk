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

package com.criteo.publisher.util.jsonadapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.lang.Boolean.parseBoolean

/**
 * Allows deserialization of Boolean value from String and Boolean json
 */
internal class BooleanJsonAdapter : JsonAdapter<Boolean>() {
  override fun fromJson(reader: JsonReader): Boolean {
    return when (reader.peek()) {
      JsonReader.Token.STRING -> parseBoolean(reader.nextString())
      JsonReader.Token.BOOLEAN -> reader.nextBoolean()
      else -> {
        throw JsonDataException(
            "Expected a string or boolean but was ${reader.peek()} at path " + reader.path)
      }
    }
  }

  override fun toJson(writer: JsonWriter, value: Boolean?) {
    if (value == null) {
      throw NullPointerException(
          "value was null! Wrap in .nullSafe() to write nullable values.")
    }
    writer.value(value)
  }

  override fun toString(): String {
    return "JsonAdapter(Boolean)"
  }
}
