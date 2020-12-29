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

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.criteo.publisher.Clock
import com.criteo.publisher.Session
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.integration.IntegrationRegistry
import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogContext
import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogLevel
import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogRecord
import com.criteo.publisher.util.AdvertisingInfo
import com.criteo.publisher.util.BuildConfigWrapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Suppress("LongParameterList")
@OpenForTesting
internal class RemoteLogRecordsFactory(
    private val buildConfigWrapper: BuildConfigWrapper,
    private val context: Context,
    private val advertisingInfo: AdvertisingInfo,
    private val session: Session,
    private val integrationRegistry: IntegrationRegistry,
    private val clock: Clock,
    private val publisherCodeRemover: PublisherCodeRemover
) {

  private val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("UTC")
  }

  /**
   * Try to create a new payload for remote logging from the given message.
   *
   * If given message has no body (no [LogMessage.message] and no [LogMessage.throwable]), then `null` is returned.
   * If given message has a log level that does not match any [RemoteLogLevel], then `null` is returned.
   */
  fun createLogRecords(logMessage: LogMessage): RemoteLogRecords? {
    val remoteLogLevel = RemoteLogLevel.fromAndroidLogLevel(logMessage.level)
    val message = createMessageBody(logMessage)

    if (remoteLogLevel == null || message == null) {
      return null
    }

    val logRecord = RemoteLogRecord(remoteLogLevel, listOf(message))

    val context = RemoteLogContext(
        buildConfigWrapper.sdkVersion,
        context.packageName,
        advertisingInfo.advertisingId,
        session.sessionId,
        integrationRegistry.profileId,
        logMessage.throwable?.javaClass?.simpleName,
        logMessage.logId
    )

    return RemoteLogRecords(context, listOf(logRecord))
  }

  @VisibleForTesting
  fun createMessageBody(logMessage: LogMessage): String? {
    if (logMessage.message == null && logMessage.throwable == null) {
      return null
    }

    val currentDate = Date(clock.currentTimeInMillis)
    val formattedDate = iso8601Format.format(currentDate)

    val messageParts = listOfNotNull(
        logMessage.message,
        logMessage.throwable?.stacktraceString,
        "threadId:${getCurrentThreadName()}",
        formattedDate
    )

    return messageParts.takeIf { it.isNotEmpty() }?.joinToString(",")
  }

  @VisibleForTesting
  fun getCurrentThreadName(): String = Thread.currentThread().name

  private val Throwable.stacktraceString get() = getStackTraceString(publisherCodeRemover.removePublisherCode(this))

  /**
   * This method is nullable because on JVM tests, methods from AndroidSDK returns null
   */
  @VisibleForTesting
  fun getStackTraceString(throwable: Throwable): String? = Log.getStackTraceString(throwable)
}
