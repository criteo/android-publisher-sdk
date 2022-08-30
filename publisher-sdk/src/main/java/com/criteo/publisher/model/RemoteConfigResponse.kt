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

import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.logging.RemoteLogRecords.RemoteLogLevel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@OpenForTesting
@JsonClass(generateAdapter = true)
data class RemoteConfigResponse(
    /**
     * The kill switch applies to both the iOS and Android SDKs, and tells the SDK to stop getting
     * bids from CDB. Once the kill switch has been set, you may get up to one bid per slot, since the
     * first call to CDB may be initiated prior to the response from the config endpoint. However,
     * that will happen only the first time the app is started after the kill switch is set, because
     * the switch value is persisted (SharedPreferences for Android, UserDefaults for iOS).
     */
    @Json(name = "killSwitch")
    val killSwitch: Boolean? = null,

    /**
     * e.g. %%displayUrl%%, replaced by the [displayUrl][CdbResponseSlot.getDisplayUrl]
     * provided by CDB, in the wrapper HTML that is loaded in a [android.webkit.WebView].
     */
    @Json(name = "AndroidDisplayUrlMacro")
    val androidDisplayUrlMacro: String? = null,
    /**
     * Wrapper HTML that will contain the displayUrl, e.g. :
     * <pre>`
     * <html>
     * <body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'>
     * <script src=\"%%displayUrl%%\">
     * </script>
     * </body>
     * </html>
    `</pre> *
     *
     *
     * In this example [%%displayURL%%][.getAndroidDisplayUrlMacro] is replaced by the
     * display url value.
     */
    @Json(name = "AndroidAdTagUrlMode")
    val androidAdTagUrlMode: String? = null,

    /**
     * e.g. %%adTagData%%, replaced by the contents of [ displayUrl][CdbResponseSlot.getDisplayUrl],
     * meaning the JavaScript code to display the ad.
     */
    @Json(name = "AndroidAdTagDataMacro")
    val androidAdTagDataMacro: String? = null,

    /**
     * Wrapper HTML that will contain the JavaScript code, e.g.
     * <pre>`
     * <html>
     * <body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'>
     * <script>%%adTagData%%</script>
     * </body>
     * </html>
    `</pre> *
     *
     *
     * In this example [%%adTagData%%][.getAndroidAdTagDataMacro] is replaced by the
     * JavaScript code provided by display url.
     */
    @Json(name = "AndroidAdTagDataMode")
    val androidAdTagDataMode: String? = null,

    /**
     * Feature flag for activating/deactivating the CSM feature. If set to `true`, then the
     * feature is activated. If `false`, then it is deactivated. If the flag is not present
     * (i.e. equals to `null`), then the previous persisted value of this flag is taken. If
     * there is no previous value, this means that this is a fresh start of a new application, then a
     * default value is taken.
     */
    @Json(name = "csmEnabled")
    val csmEnabled: Boolean? = null,

    /**
     * Feature flag for activating/deactivating the live-bidding feature. If set to `true`,
     * then the feature is activated. If `false`, then it is deactivated. If the flag is
     * not present (i.e. equals to `null`), then the previous persisted value of this flag
     * is taken. If there is no previous value, this means that this is a fresh start of a new
     * application, then a default value is taken.
     */
    @Json(name = "liveBiddingEnabled")
    val liveBiddingEnabled: Boolean? = null,

    /**
     * Amount of time (in milliseconds) given to the SDK to serve a bid to the publisher. If the SDK get a CDB response
     * within this time budget, SDK returns it directly. Else, cached bid is used (if present) and CDB response
     * is cached for later.
     */
    @Json(name = "liveBiddingTimeBudgetInMillis")
    val liveBiddingTimeBudgetInMillis: Int? = null,

    /**
     * Feature flag for activating/deactivating the prefetch during initialization. If set to `true`, then the
     * feature is activated. If `false`, then it is deactivated. If the flag is not present (i.e. equals to
     * `null`), then the previous persisted value of this flag is taken. If there is no previous value, this
     * means that this is a fresh start of a new application, then a default value is taken.
     */
    @Json(name = "prefetchOnInitEnabled")
    val prefetchOnInitEnabled: Boolean? = null,

    /**
     * Desired level of logs to get from the remote logs handler.
     *
     *
     * Logs with log level equals or greater to this would be sent remotely. Other logs are skipped.
     * Here, "greater" reflects to this order (from lower to higher):
     *
     *  * [RemoteLogLevel.DEBUG]
     *  * [RemoteLogLevel.INFO]
     *  * [RemoteLogLevel.WARNING]
     *  * [RemoteLogLevel.ERROR]
     *  * [RemoteLogLevel.NONE]
     *
     *
     * If this value is `null`, then the previous persisted value is taken. If there is no previous value, this
     * means that this is a fresh start of a new application, then a default value is taken.
     */
    @Json(name = "remoteLogLevel")
    val remoteLogLevel: RemoteLogLevel? = null
) {

  fun withKillSwitch(killSwitch: Boolean?): RemoteConfigResponse {
    return copy(killSwitch = killSwitch)
  }

  companion object {

    @JvmStatic
    fun createEmpty(): RemoteConfigResponse {
      return RemoteConfigResponse()
    }
  }
}
