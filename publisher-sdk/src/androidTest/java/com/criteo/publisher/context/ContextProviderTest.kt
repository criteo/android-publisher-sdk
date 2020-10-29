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

import android.content.res.Resources
import android.os.LocaleList
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Rule
import org.junit.Test
import java.util.Locale

class ContextProviderTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @SpyBean
  private lateinit var contextProvider: ContextProvider

  @Test
  fun fetchDeviceHardwareVersion_DoesNotThrow() {
    assertThatCode {
      contextProvider.fetchDeviceModel()
    }.doesNotThrowAnyException()
  }

  @Test
  fun fetchDeviceMake_DoesNotThrow() {
    assertThatCode {
      contextProvider.fetchDeviceMake()
    }.doesNotThrowAnyException()
  }

  @Test
  fun fetchDeviceConnectionType_DoesNotThrow() {
    assertThatCode {
      contextProvider.fetchDeviceConnectionType()
    }.doesNotThrowAnyException()
  }

  @Test
  fun fetchUserCountry_GivenNoLocale_ReturnNull() {
    Resources.getSystem().configuration.setLocales(LocaleList())

    val country = contextProvider.fetchUserCountry()

    assertThat(country).isNull()
  }

  @Test
  fun fetchUserCountry_GivenLocaleWithBlankCountry_ReturnNull() {
    Resources.getSystem().configuration.setLocales(LocaleList(Locale.FRENCH))

    val country = contextProvider.fetchUserCountry()

    assertThat(country).isNull()
  }

  @Test
  fun fetchUserCountry_GivenLocaleWithCountry_ReturnIt() {
    Resources.getSystem().configuration.setLocales(LocaleList(Locale.FRANCE))

    val country = contextProvider.fetchUserCountry()

    assertThat(country).isEqualTo("FR")
  }

  @Test
  fun fetchUserCountry_GivenManyLocales_ReturnFirstOneWithCountry() {
    Resources.getSystem().configuration.setLocales(LocaleList(Locale.FRENCH, Locale.FRANCE, Locale.CANADA))

    val country = contextProvider.fetchUserCountry()

    assertThat(country).isEqualTo("FR")
  }

  @Test
  fun fetchUserContext_GivenMockedData_PutThemInRightField() {
    contextProvider.stub {
      doReturn("deviceModel").whenever(mock).fetchDeviceModel()
      doReturn("deviceMake").whenever(mock).fetchDeviceMake()
      doReturn(42).whenever(mock).fetchDeviceConnectionType()
      doReturn("userCountry").whenever(mock).fetchUserCountry()
    }

    val expected = mapOf(
        "device.model" to "deviceModel",
        "device.make" to "deviceMake",
        "device.contype" to 42,
        "user.geo.country" to "userCountry"
    )

    val context = contextProvider.fetchUserContext()

    assertThat(context).containsExactlyInAnyOrderEntriesOf(expected)
  }

  @Test
  fun fetchUserContext_GivenNoData_PutNothing() {
    contextProvider.stub {
      doReturn(null).whenever(mock).fetchDeviceModel()
      doReturn(null).whenever(mock).fetchDeviceMake()
      doReturn(null).whenever(mock).fetchDeviceConnectionType()
      doReturn(null).whenever(mock).fetchUserCountry()
    }

    val context = contextProvider.fetchUserContext()

    assertThat(context).isEmpty()
  }
}
