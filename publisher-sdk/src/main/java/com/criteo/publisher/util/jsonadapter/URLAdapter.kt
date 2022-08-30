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
import java.io.IOException
import java.net.URL

internal class URLAdapter : JsonAdapter<URL>() {

  @Throws(IOException::class)
  override fun fromJson(reader: JsonReader): URL {
    if (reader.peek() != JsonReader.Token.STRING) {
      throw JsonDataException(
          "Expected a string but was ${reader.peek()} at path " + reader.path)
    }
    return URL(reader.nextString())
  }

  @Throws(IOException::class)
  override fun toJson(
      writer: JsonWriter,
      value: URL?
  ) {
    if (value == null) {
      throw NullPointerException(
          "value was null! Wrap in .nullSafe() to write nullable values.")
    }
    writer.value(value.toString())
  }

  override fun toString(): String {
    return "JsonAdapter(URL)"
  }
}
