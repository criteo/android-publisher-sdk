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
package com.criteo.publisher.model

import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.privacy.gdpr.GdprData
import com.criteo.publisher.util.JsonSerializer
import com.criteo.publisher.util.writeIntoString
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class CdbRequestTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var serializer: JsonSerializer

  @Test
  @Suppress("LongMethod")
  fun toJson_GivenAllInformation_MapThemToJson() {
    val request = CdbRequest(
        "myRequestId",
        Publisher(
            "myBundleId",
            "myCpId",
            mapOf(
                "content" to mapOf(
                    "url" to "https://www.criteo.com"
                ),
                "data" to mapOf(
                    "a" to listOf(1, 2),
                    "b" to 42.0
                )
            )
        ),
        User(
            null,
            null,
            null,
            mapOf(
                "data" to mapOf(
                    "a" to listOf(1, 2),
                    "b" to 42.0
                ),
                "device" to mapOf(
                    "make" to "Manufacturer",
                    "model" to "DummyModel"
                )
            )
        ),
        "1.2.3",
        456,
        GdprData("consent", true, 42),
        listOf()
    )

    val json = serializer.writeIntoString(request)

    assertThat(json).isEqualToIgnoringWhitespace(
        """
      {
        "id": "myRequestId",
        "publisher": {
          "bundleId": "myBundleId",
          "cpId": "myCpId",
          "ext": {
            "content": {
              "url": "https://www.criteo.com"
            },
            "data": {
              "a": [1, 2],
              "b": 42.0
            }
          }
        },
        "user": {
          "ext": {
            "data": {
              "a": [1, 2],
              "b": 42.0
            },
            "device": {
              "make": "Manufacturer",
              "model": "DummyModel"
            }
          },
          "deviceIdType": "gaid",
          "deviceOs": "android"
        },
        "sdkVersion": "1.2.3",
        "profileId": 456,
        "gdprConsent": {
          "consentData": "consent",
          "gdprApplies": true,
          "version": 42
        },
        "slots": []
      }
    """.trimIndent()
    )
  }

  @Test
  fun toJson_GivenNoGdpr_DoesNotMapIt() {
    val request = CdbRequest(
        "myRequestId",
        Publisher("myBundleId", "myCpId", mapOf()),
        User(null, null, null, mapOf()),
        "1.2.3",
        456,
        null,
        listOf()
    )

    val json = serializer.writeIntoString(request)

    assertThat(json).doesNotContain("gdprConsent")
  }
}
