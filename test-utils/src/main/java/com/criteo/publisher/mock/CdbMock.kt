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

import com.criteo.publisher.StubConstants.STUB_CREATIVE_IMAGE
import com.criteo.publisher.StubConstants.STUB_NATIVE_JSON
import com.criteo.publisher.TestAdUnits.BANNER_320_480
import com.criteo.publisher.TestAdUnits.BANNER_320_50
import com.criteo.publisher.TestAdUnits.INTERSTITIAL
import com.criteo.publisher.TestAdUnits.NATIVE
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.BannerAdUnit
import com.criteo.publisher.model.CdbRequest
import com.criteo.publisher.model.CdbRequestSlot
import com.criteo.publisher.model.InterstitialAdUnit
import com.criteo.publisher.model.NativeAdUnit
import com.criteo.publisher.util.JsonSerializer
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import java.net.HttpURLConnection
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class CdbMock(private val jsonSerializer: JsonSerializer) {

  companion object {
    const val TCF2_CONSENT_NOT_GIVEN = "COwJDpQOwJDpQIAAAAENAPCgAAAAAAAAAAAAAxQAgAsABiAAAAAA"
    const val TCF1_CONSENT_NOT_GIVEN = "BOnz82JOnz82JABABBFRCPgAAAAFuABABAA"

    private const val PREPROD_ZONE_ID = 0
    private const val PREPROD_TTL = 3600
    private const val PREPROD_CPM = "1.12"
    private const val PREPROD_CURRENCY = "EUR"

    private const val CONTENT_TYPE = "content-type"
  }

  private val mockWebServer = MockWebServer()
  private var simulateSlowNetwork = AtomicBoolean(false)

  val url: String
    get() = mockWebServer.url("").toString().let {
      // Remove the trailing / character
      it.substring(0, it.length - 1)
    }

  fun start() {
    mockWebServer.dispatcher = CdbMockHandler()
    mockWebServer.start()
  }

  fun simulatorSlowNetworkOnNextRequest() {
    simulateSlowNetwork.set(true)
  }

  fun shutdown() {
    mockWebServer.shutdown()
  }

  private inner class CdbMockHandler : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
      return when (request.requestUrl?.encodedPath) {
        "/csm" -> handleCsmRequest()
        "/config/app" -> handleConfigRequest()
        "/inapp/v2" -> handleBidRequest(request.body)
        "/delivery/ajs.php" -> handleCasperRequest(request)
        "/appevent/v1/2379" -> handleBearcatRequest()
        else -> MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
      }.also {
        if (simulateSlowNetwork.compareAndSet(true, false)) {
          it.throttleBody(1, 1, TimeUnit.SECONDS)
        }
      }
    }

    private fun handleBearcatRequest(): MockResponse {
      return MockResponse().setHeader(CONTENT_TYPE, "text/html")
    }

    private fun handleCsmRequest(): MockResponse {
      return MockResponse().setStatus("HTTP/1.1 204 No Content")
    }

    @Suppress("MaxLineLength")
    private fun handleConfigRequest(): MockResponse {
      return MockResponse()
          .setHeader(CONTENT_TYPE, "application/json; charset=utf-8")
          .setBody(
              """
            {
              "killSwitch": false,
              "AndroidDisplayUrlMacro": "%%displayUrl%%",
              "AndroidAdTagUrlMode": "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script src=\"%%displayUrl%%\"></script></body></html>",
              "AndroidAdTagDataMacro": "%%adTagData%%",
              "AndroidAdTagDataMode": "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script>%%adTagData%%</script></body></html>",
              "iOSDisplayUrlMacro": "%%displayUrl%%",
              "iOSWidthMacro": "%%width%%",
              "iOSAdTagUrlMode": "<!doctype html><html><head><meta charset=\"utf-8\"><style>body{margin:0;padding:0}</style><meta name=\"viewport\" content=\"width=%%width%%, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no\" ></head><body><script src=\"%%displayUrl%%\"></script></body></html>",
              "csmEnabled": true,
              "liveBiddingEnabled": false,
              "liveBiddingTimeBudgetInMillis": 8000
            }
          """
          )
    }

    private fun handleBidRequest(body: Buffer): MockResponse {
      val cdbRequest = jsonSerializer.read(CdbRequest::class.java, body.inputStream())

      if (shouldNotBid(cdbRequest)) {
        return MockResponse().setResponseCode(HttpURLConnection.HTTP_NO_CONTENT)
      }

      val responseSlots = cdbRequest.slots.mapNotNull { it.toResponseSlot() }.joinToString()
      val requestId = UUID.randomUUID().toString()
      val cdbResponse = """
      {
        "slots": [$responseSlots],
        "requestId":"$requestId"
      }
    """.trimIndent()

      return MockResponse()
          .setHeader(CONTENT_TYPE, "application/json; charset=utf-8")
          .setBody(cdbResponse)
    }

    private fun shouldNotBid(cdbRequest: CdbRequest) =
        cdbRequest.gdprData?.consentData() in setOf(TCF1_CONSENT_NOT_GIVEN, TCF2_CONSENT_NOT_GIVEN)

    private fun CdbRequestSlot.toResponseSlot(): String? {
      val size = sizes.first().toAdSize()
      val width = size.width
      val height = size.height

      val bannerAdUnit = BannerAdUnit(placementId, size)
      val interstitialAdUnit = InterstitialAdUnit(placementId)
      val nativeAdUnit = NativeAdUnit(placementId)

      return when {
        bannerAdUnit == BANNER_320_50 || bannerAdUnit == BANNER_320_480 || interstitialAdUnit == INTERSTITIAL -> {
          """
        {
          "impId": "$impressionId",
          "placementId": "$placementId",
          "arbitrageId": "arbitrage_id",
          "zoneId": $PREPROD_ZONE_ID,
          "cpm": "$PREPROD_CPM",
          "currency": "$PREPROD_CURRENCY",
          "width": $width,
          "height": $height,
          "ttl": $PREPROD_TTL,
          "displayUrl": "$url/delivery/ajs.php?width=$width&height=$height"
        }
      """.trimIndent()
        }
        nativeAdUnit == NATIVE -> {
          """
        {
          "impId": "$impressionId",
          "placementId": "$placementId",
          "arbitrageId": "",
          "zoneId": $PREPROD_ZONE_ID,
          "cpm": "$PREPROD_CPM",
          "currency": "$PREPROD_CURRENCY",
          "width": $width,
          "height": $height,
          "ttl": $PREPROD_TTL,
          "native": $STUB_NATIVE_JSON
        }
      """.trimIndent()
        }
        else -> null
      }
    }

    private fun String.toAdSize(): AdSize {
      val split = split("x")
      val width = split[0].toInt()
      val height = split[1].toInt()
      return AdSize(width, height)
    }

    private fun handleCasperRequest(request: RecordedRequest): MockResponse {
      val width = request.requestUrl!!.queryParameter("width")!!.toInt()
      val height = request.requestUrl!!.queryParameter("height")!!.toInt()

      val response = """
      (function(){
      var s = "";
      s += "<"+"a href=\"https://criteo.com\">\n";
      s += "  <"+"img width='$width' height='$height' src=\"$STUB_CREATIVE_IMAGE\"/>\n";
      s += "<"+"/a>\n";
      s += "\n";
      document.write(s);})();
    """.trimIndent()

      return MockResponse()
          .setHeader(CONTENT_TYPE, "text/plain; charset=utf-8")
          .setBody(response)
    }
  }
}
