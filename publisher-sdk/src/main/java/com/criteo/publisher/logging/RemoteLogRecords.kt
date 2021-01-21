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
import androidx.annotation.Keep
import com.criteo.publisher.annotation.OpenForTesting
import com.google.gson.annotations.SerializedName

@OpenForTesting
data class RemoteLogRecords(
    @SerializedName("context") val context: RemoteLogContext,
    @SerializedName("errors") val logRecords: List<RemoteLogRecord>
) {
  data class RemoteLogRecord(
      @SerializedName("errorType") val level: RemoteLogLevel,
      @SerializedName("messages") val messages: List<String>
  )

  @OpenForTesting
  data class RemoteLogContext(
      @SerializedName("version") val version: String,
      @SerializedName("bundleId") val bundleId: String,
      @SerializedName("deviceId") var deviceId: String?,
      @SerializedName("sessionId") val sessionId: String,
      @SerializedName("profileId") val profileId: Int,
      @SerializedName("exception") val exceptionType: String?,
      @SerializedName("logId") val logId: String?
  )

  @Keep // for serialization
  enum class RemoteLogLevel {
    @SerializedName("Debug")
    DEBUG,

    @SerializedName("Info")
    INFO,

    @SerializedName("Warning")
    WARNING,

    @SerializedName("Error")
    ERROR,

    @SerializedName("None")
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
