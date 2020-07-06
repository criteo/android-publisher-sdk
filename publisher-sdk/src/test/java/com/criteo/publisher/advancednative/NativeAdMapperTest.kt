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

package com.criteo.publisher.advancednative

import android.content.ComponentName
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.criteo.publisher.activity.TopActivityFinder
import com.criteo.publisher.adview.Redirection
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.nativeads.NativeAssets
import com.criteo.publisher.model.nativeads.NativeProduct
import com.criteo.publisher.network.PubSdkApi
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Answers
import java.lang.ref.WeakReference
import java.net.URI
import javax.inject.Inject

class NativeAdMapperTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @SpyBean
  private lateinit var runOnUiThreadExecutor: RunOnUiThreadExecutor

  @MockBean
  private lateinit var visibilityTracker: VisibilityTracker

  @MockBean
  private lateinit var clickDetection: ClickDetection

  @MockBean
  private lateinit var topActivityFinder: TopActivityFinder

  @MockBean
  private lateinit var redirection: Redirection

  @MockBean
  private lateinit var adChoiceOverlay: AdChoiceOverlay

  @MockBean
  private lateinit var rendererHelper: RendererHelper

  @MockBean
  private lateinit var api: PubSdkApi

  @Inject
  private lateinit var mapper: NativeAdMapper

  @Test
  fun map_GivenAssets_ReturnsNativeAdWithSameDataAndPreloadImages() {
    val productImageUrl = URI.create("http://click.url").toURL()
    val advertiserLogoUrl = URI.create("http://logo.url").toURL()
    val adChoiceUrl = URI.create("http://ad-choice.url").toURL()

    val product = mock<NativeProduct>() {
      on { title } doReturn "myTitle"
      on { description } doReturn "myDescription"
      on { price } doReturn "42€"
      on { callToAction } doReturn "myCTA"
      on { imageUrl } doReturn productImageUrl
    }

    val assets = mock<NativeAssets>() {
      on { this.product } doReturn product
      on { advertiserDomain } doReturn "advDomain"
      on { advertiserDescription } doReturn "advDescription"
      on { this.advertiserLogoUrl } doReturn advertiserLogoUrl
      on { privacyOptOutImageUrl } doReturn adChoiceUrl
      on { privacyLongLegalText } doReturn "longLegalText"
    }

    val nativeAd = mapper.map(assets, WeakReference(null), mock())

    assertThat(nativeAd.title).isEqualTo("myTitle")
    assertThat(nativeAd.description).isEqualTo("myDescription")
    assertThat(nativeAd.price).isEqualTo("42€")
    assertThat(nativeAd.callToAction).isEqualTo("myCTA")
    assertThat(nativeAd.productMedia).isEqualTo(CriteoMedia.create(productImageUrl))
    assertThat(nativeAd.advertiserDomain).isEqualTo("advDomain")
    assertThat(nativeAd.advertiserDescription).isEqualTo("advDescription")
    assertThat(nativeAd.advertiserLogoMedia).isEqualTo(CriteoMedia.create(advertiserLogoUrl))
    assertThat(nativeAd.legalText).isEqualTo("longLegalText")

    verify(rendererHelper).preloadMedia(productImageUrl)
    verify(rendererHelper).preloadMedia(advertiserLogoUrl)
    verify(rendererHelper).preloadMedia(adChoiceUrl)
  }

  @Test
  fun watchForImpression_GivenVisibilityTriggeredManyTimesOnDifferentViews_NotifyListenerOnceForImpressionAndFirePixels() {
    val listener = mock<CriteoNativeAdListener>()

    val pixel1 = URI.create("http://pixel1.url").toURL()
    val pixel2 = URI.create("http://pixel2.url").toURL()
    val assets = mock<NativeAssets>(defaultAnswer=Answers.RETURNS_DEEP_STUBS) {
      on { impressionPixels } doReturn listOf(pixel1, pixel2)
    }

    val view1 = mock<View>()
    val view2 = mock<View>()

    givenDirectUiExecutor()

    //when
    val nativeAd = mapper.map(assets, WeakReference(listener), mock())

    nativeAd.watchForImpression(view1)
    verify(visibilityTracker).watch(eq(view1), check {
      it.onVisible()
      mockedDependenciesRule.waitForIdleState()
      it.onVisible()
      mockedDependenciesRule.waitForIdleState()
    })

    nativeAd.watchForImpression(view2)
    verify(visibilityTracker).watch(eq(view2), check {
      it.onVisible()
      mockedDependenciesRule.waitForIdleState()
      it.onVisible()
      mockedDependenciesRule.waitForIdleState()
    })

    // then
    verify(listener, times(1)).onAdImpression()
    verify(api, times(1)).executeRawGet(pixel1)
    verify(api, times(1)).executeRawGet(pixel2)
  }

  @Test
  fun setProductClickableView_GivenDifferentViewsClickedManyTimes_NotifyListenerForClicksAndRedirectUser() {
    val listener = mock<CriteoNativeAdListener>()

    val product = mock<NativeProduct> {
      on { clickUrl } doReturn URI.create("click://uri.com")
    }

    val assets = mock<NativeAssets> {
      on { this.product } doReturn product
    }

    val view1 = mock<View>()
    val view2 = mock<View>()

    val topActivity = mock<ComponentName>()
    topActivityFinder.stub {
      on { topActivityName } doReturn topActivity
    }

    givenDirectUiExecutor()

    //when
    val nativeAd = mapper.map(assets, WeakReference(listener), mock())
    nativeAd.setProductClickableView(view1)
    nativeAd.setProductClickableView(view2)

    argumentCaptor<NativeViewClickHandler>().apply {
      verify(clickDetection, times(2)).watch(any(), capture())

      allValues.forEach {
        it.onClick()
        it.onClick()
      }
    }

    // then
    verify(listener, times(4)).onAdClicked()
    verify(redirection, times(4)).redirect(eq("click://uri.com"), eq(topActivity), any())
  }

  @Test
  fun setPrivacyOptOutClickableView_GivenDifferentViewsClickedManyTimes_NotifyListenerForClicksAndRedirectUser() {
    val listener = mock<CriteoNativeAdListener>()

    val assets = mock<NativeAssets>(defaultAnswer=Answers.RETURNS_DEEP_STUBS) {
      on { privacyOptOutClickUrl } doReturn URI.create("privacy://criteo")
    }

    val view1 = mock<View>()
    val view2 = mock<View>()

    val topActivity = mock<ComponentName>()
    topActivityFinder.stub {
      on { topActivityName } doReturn topActivity
    }

    givenDirectUiExecutor()

    //when
    val nativeAd = mapper.map(assets, WeakReference(listener), mock())
    nativeAd.setAdChoiceClickableView(view1)
    nativeAd.setAdChoiceClickableView(view2)

    argumentCaptor<NativeViewClickHandler>().apply {
      verify(clickDetection, times(2)).watch(any(), capture())

      allValues.forEach {
        it.onClick()
        it.onClick()
      }
    }

    // then
    verify(listener, never()).onAdClicked()
    verify(redirection, times(4)).redirect(eq("privacy://criteo"), eq(topActivity), any())
  }

  @Test
  fun getAdChoiceView_GivenAnyView_DelegateToAdChoiceOverlay() {
    val listener = mock<CriteoNativeAdListener>()
    val nativeAssets = mock<NativeAssets>(defaultAnswer = Answers.RETURNS_DEEP_STUBS)
    val inputView = mock<View>()
    val adChoiceView = mock<ImageView>()

    whenever(adChoiceOverlay.getAdChoiceView(inputView)).doReturn(adChoiceView)

    val nativeAd = mapper.map(nativeAssets, WeakReference(listener), mock())
    val outputView = nativeAd.getAdChoiceView(inputView)

    assertThat(outputView).isEqualTo(adChoiceView)
  }

  @Test
  fun createRenderedNativeView_GivenRenderer_InflateThenRenderAndSetupInternals() {
    val listener = mock<CriteoNativeAdListener>()
    val renderer = mock<CriteoNativeRenderer>()
    val assets = mock<NativeAssets>(defaultAnswer=Answers.RETURNS_DEEP_STUBS)
    val context = mock<Context>()
    val parent = mock<ViewGroup>()
    val nativeView = mock<ViewGroup>()
    val adChoiceView = mock<ImageView>()

    whenever(renderer.createNativeView(context, parent)).doReturn(nativeView)
    whenever(adChoiceOverlay.getAdChoiceView(nativeView)).doReturn(adChoiceView)

    val nativeAd = spy(mapper.map(assets, WeakReference(listener), renderer))
    val renderedView = nativeAd.createNativeRenderedView(context, parent)

    assertThat(renderedView).isEqualTo(nativeView)

    inOrder(renderer, nativeAd) {
      verify(renderer).renderNativeView(rendererHelper, nativeView, nativeAd)
      verify(nativeAd).watchForImpression(nativeView)
      verify(nativeAd).setProductClickableView(nativeView)
      verify(nativeAd).setAdChoiceClickableView(adChoiceView)
    }
  }

  private fun givenDirectUiExecutor() {
    runOnUiThreadExecutor.stub {
      on { executeAsync(any()) } doAnswer {
        val command: Runnable = it.getArgument(0)
        command.run()
      }
    }
  }

}