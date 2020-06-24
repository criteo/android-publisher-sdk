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

package com.criteo.publisher.mock

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.assertj.core.api.SoftAssertionsRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CdbMockTest {

  private val client = OkHttpClient()

  private val cdbUrl = "https://directbidder-test-app.par.preprod.crto.in"
  private val textPlain = "text/plain".toMediaType()

  private lateinit var cdbMock: CdbMock
  private lateinit var mockUrl: String

  @Before
  fun setUp() {
    cdbMock = CdbMock()
    cdbMock.start()
    mockUrl = cdbMock.url
  }

  @After
  fun tearDown() {
    cdbMock.shutdown()
  }

  @Test
  fun csm_GivenValidInput_BothServerReturnSame() {
    val body = """
      {
        "feedbacks": [
          {
            "slots": [
              {
                "impressionId": "5ef2fac85ef3ebf058efe7eba3a2fa52",
                "cachedBidUsed": true
              }
            ],
            "elapsed": 264,
            "isTimeout": false,
            "cdbCallStartElapsed": 0,
            "cdbCallEndElapsed": 91,
            "requestGroupId": "5ef2fac89b30fedfa1681d2c1200e8d8"
          },
          {
            "slots": [
              {
                "impressionId": "00000000aea73bb779c6aa0b20316092",
                "cachedBidUsed": true
              }
            ],
            "isTimeout": false,
            "cdbCallStartElapsed": 0,
            "cdbCallEndElapsed": 0,
            "requestGroupId": "0000000012f0a24c600fc28b15014550"
          },
          {
            "slots": [
              {
                "impressionId": "00000000d57ce2ed414b78a322c3f989",
                "cachedBidUsed": true
              }
            ],
            "elapsed": 0,
            "isTimeout": false,
            "cdbCallStartElapsed": 0,
            "cdbCallEndElapsed": 0,
            "requestGroupId": "00000000438371c9ef2ca37910979938"
          },
          {
            "slots": [
              {
                "impressionId": "5ef2fac80efac4c54d7e6e6e340160cb",
                "cachedBidUsed": false
              }
            ],
            "isTimeout": false,
            "cdbCallStartElapsed": 0,
            "cdbCallEndElapsed": 98,
            "requestGroupId": "5ef2fac8cbff01e5fbeea8050d054f00"
          },
          {
            "slots": [
              {
                "impressionId": "5ef2fac81a0cda30df12baeb0978d0f6",
                "cachedBidUsed": false
              }
            ],
            "isTimeout": true,
            "cdbCallStartElapsed": 0,
            "requestGroupId": "5ef2fac8f49aed0783285701eb233185"
          },
          {
            "slots": [
              {
                "impressionId": "5ef2fac84ca61a0ece5a2c0ce520265a",
                "cachedBidUsed": false
              }
            ],
            "isTimeout": false,
            "cdbCallStartElapsed": 0,
            "requestGroupId": "5ef2fac8c7a28ee8d6e97769cfd1a8c4"
          }
        ],
        "wrapper_version": "3.7.0",
        "profile_id": 235
      }
    """.trimIndent()

    val cdbRequest = body.toCsmRequest(cdbUrl)
    val mockRequest = body.toCsmRequest(mockUrl)

    client.newCall(cdbRequest).execute().use { cdbResponse ->
      client.newCall(mockRequest).execute().use { mockResponse ->
        assertSoftly {
          assertThat(mockResponse.code).isEqualTo(cdbResponse.code)
          assertThat(mockResponse.body?.bytes()).isEqualTo(cdbResponse.body?.bytes())

          // There is no interesting value to assert on. But we keep track of headers, in case there
          // is a change
          assertThat(cdbResponse.headers.names()).containsExactlyInAnyOrder(
              "date",
              "server",
              "timing-allow-origin",
              "vary"
          )
        }
      }
    }
  }

  private fun assertSoftly(softly: SoftAssertions.() -> Unit) {
    SoftAssertions.assertSoftly {
      softly(it)
    }
  }

  private fun String.toCsmRequest(baseUrl: String): Request {
    return Request.Builder()
        .url("$baseUrl/csm")
        .post(toRequestBody(textPlain))
        .build()
  }

}