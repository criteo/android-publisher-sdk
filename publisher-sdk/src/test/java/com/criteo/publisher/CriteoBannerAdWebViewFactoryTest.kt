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

package com.criteo.publisher

import android.content.Context
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.BannerAdUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit

class CriteoBannerAdWebViewFactoryTest {
  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  private val bannerAdUnit = BannerAdUnit("mock", AdSize(320, 50))

  @Mock
  private lateinit var context: Context

  @Mock
  private lateinit var criteo: Criteo

  @Mock
  private lateinit var criteoBannerView: CriteoBannerView

  private lateinit var factory: CriteoBannerAdWebViewFactory

  @Before
  fun setUp() {
    factory = CriteoBannerAdWebViewFactory()
  }

  @Test
  fun createWithBannerAdUnitPassed_ShouldHaveSameAdUnit() {
    val bannerAdWebView = factory.create(context, null, bannerAdUnit, criteo, criteoBannerView)

    assertThat(bannerAdWebView.bannerAdUnit).isEqualTo(bannerAdUnit)
  }

  @Test
  fun createWithBannerAdUnitAsNull_ShouldHaveSameAdUnit() {
    val bannerAdWebView = factory.create(context, null, null, criteo, criteoBannerView)

    assertThat(bannerAdWebView.bannerAdUnit).isEqualTo(null)
  }
}
