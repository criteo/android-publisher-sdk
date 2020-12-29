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

import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogContext
import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogLevel
import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogRecord
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.JsonSerializer
import com.criteo.publisher.util.writeIntoString
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class RemoteLogRecordsTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var serializer: JsonSerializer

  @Test
  @Suppress("LongMethod")
  fun write_GivenData_ReturnSerializedJson() {
    val multiLogRecords = listOf(
        RemoteLogRecords(
            RemoteLogContext(
                "1.2.3",
                "org.dummy.bundle",
                "my-device-id",
                "my-session-id",
                42,
                null,
                null
            ),
            listOf(
                RemoteLogRecord(
                    RemoteLogLevel.INFO,
                    listOf(
                        "message1",
                        "message2"
                    )
                ),
                RemoteLogRecord(
                    RemoteLogLevel.WARNING,
                    listOf("message3")
                )
            )
        ),
        RemoteLogRecords(
            RemoteLogContext(
                "4.5.6",
                "org.dummy.bundle2",
                "my-device-id2",
                "my-session-id2",
                1337,
                "NullPointerException",
                "myLogId"
            ),
            listOf(
                RemoteLogRecord(
                    RemoteLogLevel.DEBUG,
                    listOf("message4")
                ),
                RemoteLogRecord(
                    RemoteLogLevel.ERROR,
                    listOf("message5")
                )
            )
        )
    )

    val json = serializer.writeIntoString(multiLogRecords)

    assertThat(json).isEqualToIgnoringWhitespace("""
      [
        {
          "context": {
            "version": "1.2.3",
            "bundleId": "org.dummy.bundle",
            "deviceId": "my-device-id",
            "sessionId": "my-session-id",
            "profileId": 42
          },
          "errors": [
            {
              "errorType": "Info",
              "messages": [
                "message1",
                "message2"
              ]
            },
            {
              "errorType": "Warning",
              "messages": [
                "message3"
              ]
            }
          ]
        },
        {
          "context": {
            "version": "4.5.6",
            "bundleId": "org.dummy.bundle2",
            "deviceId": "my-device-id2",
            "sessionId": "my-session-id2",
            "profileId": 1337,
            "exception": "NullPointerException",
            "logId": "myLogId"
          },
          "errors": [
            {
              "errorType": "Debug",
              "messages": [
                "message4"
              ]
            },
            {
              "errorType": "Error",
              "messages": [
                "message5"
              ]
            }
          ]
        }
      ]
    """.trimIndent())
  }
}
