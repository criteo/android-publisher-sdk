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

import com.criteo.publisher.DependencyProvider
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.SoftAssertions
import org.junit.After
import org.junit.Before
import org.junit.Test

class CdbMockTest {

  private val client = OkHttpClient()

  private val cdbUrl = "https://directbidder-test-app.par.preprod.crto.in"
  private val textPlain = "text/plain".toMediaType()

  private lateinit var cdbMock: CdbMock
  private lateinit var mockUrl: String

  @Before
  fun setUp() {
    val jsonSerializer = DependencyProvider.getInstance().provideJsonSerializer()
    cdbMock = CdbMock(jsonSerializer)
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
          assertThat(mockResponse.body?.string()).isEqualTo(cdbResponse.body?.string())
          assertItContainsExpectedCdbKeys(cdbResponse.headers)
        }
      }
    }
  }

  @Test
  fun config_GivenValidInputWithoutSpecialConfig_BothServerReturnSame() {
    val body = """
      {
        "cpId": "B-000001",
        "bundleId": "com.criteo.publisher.tests.test",
        "sdkVersion": "3.6.1",
        "rtbProfileId": 235
      }
    """.trimIndent()

    val cdbRequest = body.toConfigRequest(cdbUrl)
    val mockRequest = body.toConfigRequest(mockUrl)

    client.newCall(cdbRequest).execute().use { cdbResponse ->
      client.newCall(mockRequest).execute().use { mockResponse ->
        assertSoftly {
          assertThat(mockResponse.code).isEqualTo(cdbResponse.code)
          assertThat(mockResponse.body?.string()).isEqualToIgnoringWhitespace(cdbResponse.body?.string())
          assertThat(mockResponse.headers["content-type"]).isEqualTo(cdbResponse.headers["content-type"])
          assertItContainsExpectedCdbKeys(cdbResponse.headers, contentType=true)
        }
      }
    }
  }

  @Test
  fun bid_GivenValidAndInvalidAdUnit_BothServerReturnSame() {
    val body = """
      {
        "id": "myRequestId",
        "user": {
          "deviceId": "b171073b-b504-4267-95b7-a79c9e511361",
          "deviceIdType": "gaid",
          "deviceOs": "android"
        },
        "publisher": {
          "bundleId": "com.criteo.publisher.test",
          "cpId": "B-000001"
        },
        "sdkVersion": "3.6.0",
        "profileId": 235,
        "slots": [
          {
            "impId": "5ecb8ae9cfd9dd5ed1fa238cd61be691",
            "placementId": "test-PubSdk-Base",
            "sizes": [
              "320x50"
            ]
          },
          {
            "impId": "5ecb8ae9cfd9dd5ed1fa238cd61be692",
            "placementId": "test-PubSdk-Base",
            "sizes": [
              "321x51"
            ]
          },
          {
            "impId": "5ecb8ae9cfd9dd5ed1fa238cd61be693",
            "placementId": "test-PubSdk-Interstitial",
            "interstitial": true,
            "sizes": [
              "42x24"
            ]
          },
          {
            "impId": "5ecb8ae9cfd9dd5ed1fa238cd61be694",
            "placementId": "test-PubSdk-Unknown",
            "sizes": [
              "320x50"
            ]
          },
          {
            "impId": "5ecb8ae9cfd9dd5ed1fa238cd61be695",
            "placementId": "test-PubSdk-Native",
            "isNative": true,
            "sizes": [
              "2x2"
            ]
          }
        ]
      }
    """.trimIndent()

    val cdbRequest = body.toBidRequest(cdbUrl)
    val mockRequest = body.toBidRequest(mockUrl)

    client.newCall(cdbRequest).execute().use { cdbResponse ->
      client.newCall(mockRequest).execute().use { mockResponse ->
        assertSoftly {
          assertThat(mockResponse.code).isEqualTo(cdbResponse.code)
          assertThat(mockResponse.body?.string()?.normalizeBid()).isEqualToIgnoringWhitespace(cdbResponse.body?.string()?.normalizeBid())
          assertThat(mockResponse.headers["content-type"]).isEqualTo(cdbResponse.headers["content-type"])
          assertItContainsExpectedCdbKeys(cdbResponse.headers, contentType=true)
        }
      }
    }
  }

  private fun assertSoftly(softly: SoftAssertions.() -> Unit) {
    SoftAssertions.assertSoftly {
      softly(it)
    }
  }

  private fun String.toCsmRequest(baseUrl: String) = toCdbRequest("$baseUrl/csm")
  private fun String.toConfigRequest(baseUrl: String) = toCdbRequest("$baseUrl/config/app")
  private fun String.toBidRequest(baseUrl: String) = toCdbRequest("$baseUrl/inapp/v2")

  private fun String.toCdbRequest(url: String): Request {
    return Request.Builder()
        .url(url)
        .post(toRequestBody(textPlain))
        .build()
  }

  private fun SoftAssertions.assertItContainsExpectedCdbKeys(headers: Headers, contentType: Boolean = false) {
    // There is no interesting value to assert on. But we keep track of headers, in case there
    // is a change
    val expected = mutableSetOf(
        "date",
        "server",
        "timing-allow-origin",
        "vary"
    )

    if (contentType) {
      expected.add("content-type")
    }

    assertThat(headers.names()).isEqualTo(expected)
  }

  private fun String.normalizeBid(): String {
    return replace(""""requestId":"[0-9a-f-]{36}"""".toRegex(), """"requestId": "dummy"""")
  }

}