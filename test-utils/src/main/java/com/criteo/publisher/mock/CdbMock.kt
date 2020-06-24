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

import okhttp3.Protocol
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class CdbMock {

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

}