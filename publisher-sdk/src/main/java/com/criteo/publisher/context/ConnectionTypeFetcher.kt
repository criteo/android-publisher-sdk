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

package com.criteo.publisher.context

import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.logging.LoggerFactory

@OpenForTesting
internal class ConnectionTypeFetcher(
    private val context: Context
) {

  private val logger = LoggerFactory.getLogger(ConnectionTypeFetcher::class.java)

  @SuppressLint("NewApi")
  internal fun fetchConnectionType(): ConnectionType? {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
        as? ConnectivityManager ?: return null

    return try {
      // Prefer deprecated way because it doesn't require the dangerous READ_PHONE_STATE permission.
      fetchDeprecatedDeviceConnectionType(connectivityManager)
    } catch (e: LinkageError) {
      logger.debug("Deprecated way to get connection type is not available, fallback on new API", e)
      fetchNewDeviceConnectionType(connectivityManager)
    }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private fun fetchNewDeviceConnectionType(connectivityManager: ConnectivityManager): ConnectionType? {
    val network = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

    return when {
      networkCapabilities == null -> null
      isWired(networkCapabilities) -> ConnectionType.WIRED
      isWifi(networkCapabilities) -> ConnectionType.WIFI
      isCellular(networkCapabilities) -> {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        fetchNewCellularConnectionType(telephonyManager)
      }
      else -> null
    }
  }

  @VisibleForTesting
  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  fun isWired(networkCapabilities: NetworkCapabilities): Boolean {
    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
  }

  @VisibleForTesting
  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  fun isWifi(networkCapabilities: NetworkCapabilities): Boolean {
    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
  }

  @VisibleForTesting
  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  fun isCellular(networkCapabilities: NetworkCapabilities): Boolean {
    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
  }

  @Suppress("DEPRECATION")
  private fun fetchDeprecatedDeviceConnectionType(connectivityManager: ConnectivityManager): ConnectionType? {
    val networkInfo = connectivityManager.activeNetworkInfo
    return when (networkInfo?.type) {
      ConnectivityManager.TYPE_ETHERNET -> ConnectionType.WIRED
      ConnectivityManager.TYPE_WIFI -> ConnectionType.WIFI
      ConnectivityManager.TYPE_MOBILE -> networkInfo.subtype.toConnectionType()
      else -> null
    }
  }

  // The SDK won't ask for the permission. It uses it only if publisher's app already got granted for it.
  @SuppressLint("MissingPermission")
  @VisibleForTesting
  internal fun fetchNewCellularConnectionType(telephonyManager: TelephonyManager?): ConnectionType {
    return if (telephonyManager == null || !checkReadPhoneStatePermission()) {
      // Impossible to get a more precise information without having the dangerous READ_PHONE_STATE permission
      ConnectionType.CELLULAR_UNKNOWN
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      telephonyManager.dataNetworkType.toConnectionType()
    } else {
      telephonyManager.networkType.toConnectionType()
    }
  }

  private fun checkReadPhoneStatePermission(): Boolean {
    return ContextCompat.checkSelfPermission(context, READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
  }

  /**
   * Like [TelephonyManager#getNetworkClass] which has `@UnsupportedAppUsage`
   */
  @Suppress("MagicNumber")
  private fun Int.toConnectionType(): ConnectionType {
    return when (this) {
      TelephonyManager.NETWORK_TYPE_GPRS,
      TelephonyManager.NETWORK_TYPE_EDGE,
      TelephonyManager.NETWORK_TYPE_CDMA,
      TelephonyManager.NETWORK_TYPE_1xRTT,
      TelephonyManager.NETWORK_TYPE_IDEN,
      TelephonyManager.NETWORK_TYPE_GSM
      -> ConnectionType.CELLULAR_2G
      TelephonyManager.NETWORK_TYPE_UMTS,
      TelephonyManager.NETWORK_TYPE_EVDO_0,
      TelephonyManager.NETWORK_TYPE_EVDO_A,
      TelephonyManager.NETWORK_TYPE_HSDPA,
      TelephonyManager.NETWORK_TYPE_HSUPA,
      TelephonyManager.NETWORK_TYPE_HSPA,
      TelephonyManager.NETWORK_TYPE_EVDO_B,
      TelephonyManager.NETWORK_TYPE_EHRPD,
      TelephonyManager.NETWORK_TYPE_HSPAP,
      TelephonyManager.NETWORK_TYPE_TD_SCDMA
      -> ConnectionType.CELLULAR_3G
      TelephonyManager.NETWORK_TYPE_LTE,
      TelephonyManager.NETWORK_TYPE_IWLAN,
      19 // TelephonyManager.NETWORK_TYPE_LTE_CA which has @UnsupportedAppUsage
      -> ConnectionType.CELLULAR_4G
      TelephonyManager.NETWORK_TYPE_NR -> ConnectionType.CELLULAR_5G
      else -> ConnectionType.CELLULAR_UNKNOWN
    }
  }

  @VisibleForTesting
  @Suppress("MagicNumber")
  internal enum class ConnectionType(val openRtbValue: Int) {
    WIRED(1),
    WIFI(2),
    CELLULAR_UNKNOWN(3),
    CELLULAR_2G(4),
    CELLULAR_3G(5),
    CELLULAR_4G(6),
    CELLULAR_5G(7)
  }
}
