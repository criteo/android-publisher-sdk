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
import com.criteo.publisher.context.ContextData
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.model.AdSize
import com.criteo.publisher.model.BannerAdUnit
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CriteoBannerViewTest {

  @Rule
  @JvmField
  var mockedDependenciesRule = MockedDependenciesRule()

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Mock
  private lateinit var context: Context

  @Mock
  private lateinit var criteo: Criteo

  @Mock
  private lateinit var adWebView: CriteoBannerAdWebView

  @Mock
  private lateinit var contextData: ContextData

  @Mock
  private lateinit var bid: Bid

  @MockBean
  private lateinit var criteoBannerAdWebViewFactory: CriteoBannerAdWebViewFactory

  private lateinit var bannerAdUnit: BannerAdUnit

  private lateinit var bannerView: CriteoBannerView

  @Before
  fun setUp() {
    whenever(
        criteoBannerAdWebViewFactory.create(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull()
        )
    ).thenReturn(
        adWebView
    )
    bannerAdUnit = BannerAdUnit("mock", AdSize(320, 50))

    bannerView = spy(CriteoBannerView(context, bannerAdUnit, criteo))
  }

  @Test
  fun setCriteoBannerAdListener_shouldDelegateToAdWebView() {
    val listener = mock<CriteoBannerAdListener>()
    bannerView.setCriteoBannerAdListener(listener)

    verify(adWebView).setCriteoBannerAdListener(listener)
  }

  @Test
  fun getCriteoBannerAdListener_shouldDelegateToAdWebView() {
    bannerView.getCriteoBannerAdListener()

    verify(adWebView).getCriteoBannerAdListener()
  }

  @Test
  fun loadAdWithoutParams_shouldDelegateToAdWebView() {
    bannerView.loadAd()

    verify(adWebView).loadAd(ContextData())
  }

  @Test
  fun loadAdWithContextData_shouldDelegateToAdWebView() {
    bannerView.loadAd(contextData)

    verify(adWebView).loadAd(contextData)
  }

  @Test
  fun loadAdWithBid_shouldDelegateToAdWebView() {
    bannerView.loadAd(bid)

    verify(adWebView).loadAd(bid)
  }

  @Test
  fun loadAdWithNullBid_shouldDelegateToAdWebView() {
    bannerView.loadAd(null)

    verify(adWebView).loadAd(null)
  }

  @Test
  fun destroy_shouldDelegateToAdWebView() {
    bannerView.destroy()

    verify(adWebView).destroy()
    verify(bannerView).removeAllViews()
  }
}
