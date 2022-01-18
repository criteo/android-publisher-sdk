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
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_BLUETOOTH
import android.net.ConnectivityManager.TYPE_ETHERNET
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_VPN
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.NETWORK_TYPE_NR
import android.telephony.TelephonyManager.NETWORK_TYPE_UNKNOWN
import com.criteo.publisher.context.ConnectionTypeFetcher.ConnectionType
import com.criteo.publisher.context.ConnectionTypeFetcher.ConnectionType.CELLULAR_2G
import com.criteo.publisher.context.ConnectionTypeFetcher.ConnectionType.CELLULAR_3G
import com.criteo.publisher.context.ConnectionTypeFetcher.ConnectionType.CELLULAR_4G
import com.criteo.publisher.context.ConnectionTypeFetcher.ConnectionType.CELLULAR_5G
import com.criteo.publisher.context.ConnectionTypeFetcher.ConnectionType.CELLULAR_UNKNOWN
import com.criteo.publisher.context.ConnectionTypeFetcher.ConnectionType.WIFI
import com.criteo.publisher.context.ConnectionTypeFetcher.ConnectionType.WIRED
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.whenever

@Suppress("DEPRECATION")
class ConnectionTypeFetcherTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @SpyBean
  private lateinit var context: Context

  @SpyBean
  private lateinit var connectionTypeFetcher: ConnectionTypeFetcher

  @Test
  fun fetchConnectionType_GivenNoMock_DoesNotThrow() {
    assertThatCode {
      connectionTypeFetcher.fetchConnectionType()
    }.doesNotThrowAnyException()
  }

  @Test
  fun fetchConnectionType_GivenNoConnectivityService_ReturnNull() {
    doReturn(null).whenever(context).getSystemService(any())

    val connectionType = connectionTypeFetcher.fetchConnectionType()

    assertThat(connectionType).isNull()
  }

  @Test
  fun fetchConnectionType_DeprecatedWayWorking_NoActiveNetwork_ReturnNull() {
    givenMockedConnectivityService {
      on { activeNetworkInfo } doReturn null
    }

    val connectionType = connectionTypeFetcher.fetchConnectionType()

    assertThat(connectionType).isNull()
  }

  @Test
  fun fetchConnectionType_DeprecatedWayWorking_ActiveNetwork_ReturnExpected() {
    fetchConnectionType_DeprecatedWayWorking_ActiveNetwork_ReturnExpected(TYPE_ETHERNET, expected = WIRED)
    fetchConnectionType_DeprecatedWayWorking_ActiveNetwork_ReturnExpected(TYPE_WIFI, expected = WIFI)
    fetchConnectionType_DeprecatedWayWorking_ActiveNetwork_ReturnExpected(TYPE_BLUETOOTH, expected = null)
    fetchConnectionType_DeprecatedWayWorking_ActiveNetwork_ReturnExpected(TYPE_VPN, expected = null)
    fetchConnectionType_DeprecatedWayWorking_ActiveNetwork_ReturnExpected(-1 /* TYPE_NONE */, expected = null)
    fetchConnectionType_DeprecatedWayWorking_ActiveNetwork_ReturnExpected(
        TYPE_MOBILE,
        NETWORK_TYPE_UNKNOWN,
        expected = CELLULAR_UNKNOWN
    )

    cellular2GTypes.forEach {
      fetchConnectionType_DeprecatedWayWorking_ActiveNetwork_ReturnExpected(TYPE_MOBILE, it, expected = CELLULAR_2G)
    }

    cellular3GTypes.forEach {
      fetchConnectionType_DeprecatedWayWorking_ActiveNetwork_ReturnExpected(TYPE_MOBILE, it, expected = CELLULAR_3G)
    }

    cellular4GTypes.forEach {
      fetchConnectionType_DeprecatedWayWorking_ActiveNetwork_ReturnExpected(TYPE_MOBILE, it, expected = CELLULAR_4G)
    }

    fetchConnectionType_DeprecatedWayWorking_ActiveNetwork_ReturnExpected(
        TYPE_MOBILE,
        NETWORK_TYPE_NR,
        expected = CELLULAR_5G
    )
  }

  private fun fetchConnectionType_DeprecatedWayWorking_ActiveNetwork_ReturnExpected(
      type: Int,
      subType: Int = -1,
      expected: ConnectionType?
  ) {
    val networkInfo = mock<NetworkInfo>() {
      on { this.type } doReturn type
      on { this.subtype } doReturn subType
    }

    givenMockedConnectivityService {
      on { activeNetworkInfo } doReturn networkInfo
    }

    val connectionType = connectionTypeFetcher.fetchConnectionType()

    assertThat(connectionType).isEqualTo(expected)
  }

  @Test
  fun fetchConnectionType_DeprecatedWayNotWorking_NoNetworkCapabilities_ReturnNull() {
    val network = mock<Network>()

    givenMockedConnectivityService {
      on { activeNetwork } doReturn network
      on { getNetworkCapabilities(network) } doReturn null
    }

    givenDeprecatedWayNotWorking()

    val connectionType = connectionTypeFetcher.fetchConnectionType()

    assertThat(connectionType).isNull()
  }

  @Test
  fun fetchConnectionType_DeprecatedWayNotWorking_NoExpectedCapabilities_ReturnNull() {
    val network = mock<Network>()
    val networkCapabilities = NetworkCapabilities(null)

    givenMockedConnectivityService {
      on { activeNetwork } doReturn network
      on { getNetworkCapabilities(network) } doReturn networkCapabilities
    }

    givenDeprecatedWayNotWorking()

    val connectionType = connectionTypeFetcher.fetchConnectionType()

    assertThat(connectionType).isNull()
  }

  @Test
  fun fetchConnectionType_DeprecatedWayNotWorking_EthernetCapabilities_ReturnWired() {
    val network = mock<Network>()
    val networkCapabilities = NetworkCapabilities(null)
    doReturn(true).whenever(connectionTypeFetcher).isWired(networkCapabilities) // wired is prio against wifi/cellular
    doReturn(true).whenever(connectionTypeFetcher).isWifi(networkCapabilities)
    doReturn(true).whenever(connectionTypeFetcher).isCellular(networkCapabilities)

    givenMockedConnectivityService {
      on { activeNetwork } doReturn network
      on { getNetworkCapabilities(network) } doReturn networkCapabilities
    }

    givenDeprecatedWayNotWorking()

    val connectionType = connectionTypeFetcher.fetchConnectionType()

    assertThat(connectionType).isEqualTo(WIRED)
  }

  @Test
  fun fetchConnectionType_DeprecatedWayNotWorking_WifiCapabilities_ReturnWifi() {
    val network = mock<Network>()
    val networkCapabilities = NetworkCapabilities(null)
    doReturn(true).whenever(connectionTypeFetcher).isWifi(networkCapabilities) // wifi is prio against cellular
    doReturn(true).whenever(connectionTypeFetcher).isCellular(networkCapabilities)

    givenMockedConnectivityService {
      on { activeNetwork } doReturn network
      on { getNetworkCapabilities(network) } doReturn networkCapabilities
    }

    givenDeprecatedWayNotWorking()

    val connectionType = connectionTypeFetcher.fetchConnectionType()

    assertThat(connectionType).isEqualTo(WIFI)
  }

  @Test
  fun fetchConnectionType_DeprecatedWayNotWorking_CellularCapabilities_UseNewWayOfGettingCellularConnectionType() {
    val network = mock<Network>()
    val networkCapabilities = NetworkCapabilities(null)
    doReturn(true).whenever(connectionTypeFetcher).isCellular(networkCapabilities)

    givenMockedConnectivityService {
      on { activeNetwork } doReturn network
      on { getNetworkCapabilities(network) } doReturn networkCapabilities
    }

    givenDeprecatedWayNotWorking()
    doReturn(CELLULAR_5G).whenever(connectionTypeFetcher).fetchNewCellularConnectionType(any())

    val connectionType = connectionTypeFetcher.fetchConnectionType()

    assertThat(connectionType).isEqualTo(CELLULAR_5G)
  }

  @Test
  fun fetchNewCellularConnectionType_NoTelephony_ReturnUnknownCellular() {
    val connectionType = connectionTypeFetcher.fetchNewCellularConnectionType(null)

    assertThat(connectionType).isEqualTo(CELLULAR_UNKNOWN)
  }

  @Test
  fun fetchNewCellularConnectionType_NoPermission_ReturnUnknownCellular() {
    val telephonyManager = mock<TelephonyManager>()

    doReturn(PackageManager.PERMISSION_DENIED).whenever(context).checkPermission(eq(READ_PHONE_STATE), any(), any())

    val connectionType = connectionTypeFetcher.fetchNewCellularConnectionType(telephonyManager)

    assertThat(connectionType).isEqualTo(CELLULAR_UNKNOWN)
    verifyNoInteractions(telephonyManager)
  }

  @Test
  fun fetchNewCellularConnectionType_HavingPermission_ReturnExpectedCellularType() {
    fetchNewCellularConnectionType_HavingPermission_ReturnExpectedCellularType(
        NETWORK_TYPE_UNKNOWN,
        expected = CELLULAR_UNKNOWN
    )

    cellular2GTypes.forEach {
      fetchNewCellularConnectionType_HavingPermission_ReturnExpectedCellularType(it, expected = CELLULAR_2G)
    }

    cellular3GTypes.forEach {
      fetchNewCellularConnectionType_HavingPermission_ReturnExpectedCellularType(it, expected = CELLULAR_3G)
    }

    cellular4GTypes.forEach {
      fetchNewCellularConnectionType_HavingPermission_ReturnExpectedCellularType(it, expected = CELLULAR_4G)
    }

    fetchNewCellularConnectionType_HavingPermission_ReturnExpectedCellularType(
        NETWORK_TYPE_NR,
        expected = CELLULAR_5G
    )
  }

  private fun fetchNewCellularConnectionType_HavingPermission_ReturnExpectedCellularType(
      networkType: Int,
      expected: ConnectionType
  ) {
    val telephonyManager = mock<TelephonyManager>() {
      on { dataNetworkType } doReturn networkType
      on { getNetworkType() } doReturn networkType
    }

    doReturn(PackageManager.PERMISSION_GRANTED).whenever(context).checkPermission(eq(READ_PHONE_STATE), any(), any())

    val connectionType = connectionTypeFetcher.fetchNewCellularConnectionType(telephonyManager)

    assertThat(connectionType).isEqualTo(expected)
  }

  private fun givenMockedConnectivityService(
      stubbing: KStubbing<ConnectivityManager>.(ConnectivityManager) -> Unit = {}
  ): ConnectivityManager {
    val mockedService = mock(stubbing = stubbing)
    doReturn(mockedService).whenever(context).getSystemService(Context.CONNECTIVITY_SERVICE)
    return mockedService
  }

  private fun givenDeprecatedWayNotWorking() {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    manager.stub {
      on { activeNetworkInfo } doThrow NoSuchMethodError::class
    }
  }

  private companion object {
    val cellular2GTypes = listOf(
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN,
        TelephonyManager.NETWORK_TYPE_GSM
    )

    val cellular3GTypes = listOf(
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
    )

    val cellular4GTypes = listOf(
        TelephonyManager.NETWORK_TYPE_LTE,
        TelephonyManager.NETWORK_TYPE_IWLAN,
        19
    )
  }
}
