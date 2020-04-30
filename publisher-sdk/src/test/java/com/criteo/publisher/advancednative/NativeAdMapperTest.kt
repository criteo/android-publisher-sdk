package com.criteo.publisher.advancednative

import com.criteo.publisher.model.nativeads.NativeAssets
import com.criteo.publisher.model.nativeads.NativeProduct
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.URI

class NativeAdMapperTest {

  private val mapper = NativeAdMapper()

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

    val nativeAd = mapper.map(assets)

    assertThat(nativeAd.title).isEqualTo("myTitle")
    assertThat(nativeAd.description).isEqualTo("myDescription")
    assertThat(nativeAd.price).isEqualTo("42€")
    assertThat(nativeAd.callToAction).isEqualTo("myCTA")
    assertThat(nativeAd.productImageUrl).isEqualTo(URI.create("http://click.url").toURL())
    assertThat(nativeAd.advertiserDomain).isEqualTo("advDomain")
    assertThat(nativeAd.advertiserDescription).isEqualTo("advDescription")
    assertThat(nativeAd.advertiserLogoImageUrl).isEqualTo(URI.create("http://logo.url").toURL())
  }

}