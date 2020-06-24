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

import com.criteo.publisher.StubConstants.STUB_NATIVE_JSON
import com.criteo.publisher.TestAdUnits.*
import com.criteo.publisher.model.*
import com.criteo.publisher.util.JsonSerializer
import okhttp3.Protocol
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import java.util.*

class CdbMock(private val jsonSerializer: JsonSerializer) {

  private val mockWebServer = MockWebServer()

  val url: String
    get() = mockWebServer.url("").toString().let {
      // Remove the trailing / character
      it.substring(0, it.length - 1)
    }

  fun start() {
    mockWebServer.dispatcher = object: Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse {
        return when (request.path) {
          "/csm" -> handleCsmRequest()
          "/config/app" -> handleConfigRequest()
          "/inapp/v2" -> handleBidRequest(request.body)
          else -> MockResponse().setResponseCode(404)
        }
      }
    }

    mockWebServer.start()
  }

  fun shutdown() {
    mockWebServer.shutdown()
  }

  private fun handleCsmRequest(): MockResponse {
    return MockResponse().setStatus("HTTP/1.1 204 No Content")
  }

  private fun handleConfigRequest(): MockResponse {
    return MockResponse()
        .setHeader("content-type", "application/json; charset=utf-8")
        .setBody("""
            {
              "killSwitch": false,
              "AndroidDisplayUrlMacro": "%%displayUrl%%",
              "AndroidAdTagUrlMode": "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script src=\"%%displayUrl%%\"></script></body></html>",
              "AndroidAdTagDataMacro": "%%adTagData%%",
              "AndroidAdTagDataMode": "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script>%%adTagData%%</script></body></html>",
              "iOSDisplayUrlMacro": "%%displayUrl%%",
              "iOSWidthMacro": "%%width%%",
              "iOSAdTagUrlMode": "<!doctype html><html><head><meta charset=\"utf-8\"><style>body{margin:0;padding:0}</style><meta name=\"viewport\" content=\"width=%%width%%, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no\" ></head><body><script src=\"%%displayUrl%%\"></script></body></html>",
              "csmEnabled": true
            }
          """)
  }

  private fun handleBidRequest(body: Buffer): MockResponse {
    val cdbRequest = jsonSerializer.read(CdbRequest::class.java, body.inputStream())

    val responseSlots = cdbRequest.slots.mapNotNull { it.toResponseSlot() }.joinToString()
    val requestId = UUID.randomUUID().toString()
    val cdbResponse = """
      {
        "slots": [$responseSlots],
        "requestId":"$requestId"
      }
    """.trimIndent()

    return MockResponse()
        .setHeader("content-type", "application/json; charset=utf-8")
        .setBody(cdbResponse)
  }

  private fun CdbRequestSlot.toResponseSlot(): String? {
    val size = sizes.first().toAdSize()
    val width = size.width
    val height = size.height

    val bannerAdUnit = BannerAdUnit(placementId, size)
    val interstitialAdUnit = InterstitialAdUnit(placementId)
    val nativeAdUnit = NativeAdUnit(placementId)

    if (bannerAdUnit == BANNER_320_50 || bannerAdUnit == BANNER_320_480 || interstitialAdUnit == INTERSTITIAL) {
      return """
        {
          "impId": "$impressionId",
          "placementId": "$placementId",
          "arbitrageId": "arbitrage_id",
          "cpm": "1.12",
          "currency": "EUR",
          "width": $width,
          "height": $height,
          "ttl": 0,
          "displayUrl": "https://directbidder-stubs.par.preprod.crto.in/delivery/ajs.php?width=$width&height=$height"
        }
      """.trimIndent()
    }

    if (nativeAdUnit == NATIVE) {
      return """
        {
          "impId": "$impressionId",
          "placementId": "$placementId",
          "arbitrageId": "",
          "cpm": "1.12",
          "currency": "EUR",
          "width": $width,
          "height": $height,
          "ttl": 0,
          "native": $STUB_NATIVE_JSON
        }
      """.trimIndent()
    }

    return null
  }

  private fun String.toAdSize(): AdSize {
    val split = split("x")
    val width = split[0].toInt()
    val height = split[1].toInt()
    return AdSize(width, height)
  }

}