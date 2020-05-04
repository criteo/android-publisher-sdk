package com.criteo.publisher.advancednative

import android.view.View
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.model.nativeads.NativeAssets
import com.criteo.publisher.model.nativeads.NativeProduct
import com.criteo.publisher.network.PubSdkApi
import com.criteo.publisher.util.RunOnUiThreadExecutor
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
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
  private lateinit var api: PubSdkApi

  @Inject
  private lateinit var mapper: NativeAdMapper

  @Test
  fun map_GivenAssets_ReturnsNativeAdWithSameData() {
    val product = mock<NativeProduct>() {
      on { title } doReturn "myTitle"
      on { description } doReturn "myDescription"
      on { price } doReturn "42€"
      on { callToAction } doReturn "myCTA"
      on { imageUrl } doReturn URI.create("http://click.url").toURL()
    }

    val assets = mock<NativeAssets>() {
      on { this.product } doReturn product
      on { advertiserDomain } doReturn "advDomain"
      on { advertiserDescription } doReturn "advDescription"
      on { advertiserLogoUrl } doReturn URI.create("http://logo.url").toURL()
    }

    val nativeAd = mapper.map(assets, WeakReference(null))

    assertThat(nativeAd.title).isEqualTo("myTitle")
    assertThat(nativeAd.description).isEqualTo("myDescription")
    assertThat(nativeAd.price).isEqualTo("42€")
    assertThat(nativeAd.callToAction).isEqualTo("myCTA")
    assertThat(nativeAd.productImageUrl).isEqualTo(URI.create("http://click.url").toURL())
    assertThat(nativeAd.advertiserDomain).isEqualTo("advDomain")
    assertThat(nativeAd.advertiserDescription).isEqualTo("advDescription")
    assertThat(nativeAd.advertiserLogoImageUrl).isEqualTo(URI.create("http://logo.url").toURL())
  }

  @Test
  fun watchForImpression_GivenVisibilityTriggeredManyTimesOnDifferentViews_NotifyListenerOnceForImpressionAndFirePixels() {
    val listener = mock<CriteoNativeAdListener>()

    val pixel1 = URI.create("http://pixel1.url").toURL()
    val pixel2 = URI.create("http://pixel2.url").toURL()
    val assets = mock<NativeAssets>() {
      on { impressionPixels } doReturn listOf(pixel1, pixel2)
    }

    val view1 = mock<View>()
    val view2 = mock<View>()

    runOnUiThreadExecutor.stub {
      on { executeAsync(any()) } doAnswer {
        val command: Runnable = it.getArgument(0)
        command.run()
      }
    }

    //when
    val nativeAd = mapper.map(assets, WeakReference(listener))

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

}