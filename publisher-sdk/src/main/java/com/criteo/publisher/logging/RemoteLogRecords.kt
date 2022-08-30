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

package com.criteo.publisher.logging

import android.util.Log
import com.criteo.publisher.annotation.OpenForTesting
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@OpenForTesting
@JsonClass(generateAdapter = true)
data class RemoteLogRecords(
    @Json(name = "context") val context: RemoteLogContext,
    @Json(name = "errors") val logRecords: List<RemoteLogRecord>
) {
  @JsonClass(generateAdapter = true)
  data class RemoteLogRecord(
      @Json(name = "errorType") val level: RemoteLogLevel,
      @Json(name = "messages") val messages: List<String>
  )

  @OpenForTesting
  @JsonClass(generateAdapter = true)
  data class RemoteLogContext(
      @Json(name = "version") val version: String,
      @Json(name = "bundleId") val bundleId: String,
      @Json(name = "deviceId") var deviceId: String?,
      @Json(name = "sessionId") val sessionId: String,
      @Json(name = "profileId") val profileId: Int,
      @Json(name = "exception") val exceptionType: String?,
      @Json(name = "logId") val logId: String?,
      @Json(name = "deviceOs") val deviceOs: String?
  )

  enum class RemoteLogLevel {
    @Json(name = "Debug")
    DEBUG,

    @Json(name = "Info")
    INFO,

    @Json(name = "Warning")
    WARNING,

    @Json(name = "Error")
    ERROR,

    @Json(name = "None")
    NONE;

    companion object {
      fun fromAndroidLogLevel(logLevel: Int): RemoteLogLevel? = when (logLevel) {
        Log.DEBUG -> DEBUG
        Log.INFO -> INFO
        Log.WARN -> WARNING
        Log.ERROR -> ERROR
        else -> null
      }
    }
  }
}
