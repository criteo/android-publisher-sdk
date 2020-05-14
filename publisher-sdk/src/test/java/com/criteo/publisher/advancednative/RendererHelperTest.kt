package com.criteo.publisher.advancednative

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test
import java.net.URI

class RendererHelperTest {

  private val uiExecutor = DirectMockRunOnUiThreadExecutor()

  @Test
  fun setMediaInView_GivenImageLoader_DelegateToIt() {
    val url = URI.create("http://image.url").toURL()
    val media = CriteoMedia.create(url)
    val imageLoader = mock<ImageLoader>()
    val helper = RendererHelper(imageLoader, uiExecutor)

    doAnswer {
      uiExecutor.expectIsRunningInExecutor()
      null
    }.whenever(imageLoader).loadImageInto(any(), any(), any())
    val imageView = mock<ImageView>()
    val placeholder = mock<Drawable>()
    val mediaView = mock<CriteoMediaView>() {
      on { getImageView() } doReturn imageView
      on { getPlaceholder() } doReturn placeholder
    }

    helper.setMediaInView(media, mediaView)

    verify(imageLoader).loadImageInto(url, imageView, placeholder)
    uiExecutor.expectIsRunningInExecutor()
  }

  @Test
  fun setMediaInView_GivenImageLoaderThrowing_CatchTheException() {
    val imageLoader = mock<ImageLoader>() {
      doThrow(Exception::class).whenever(mock).loadImageInto(any(), any(), any())
    }

    val helper = RendererHelper(imageLoader, uiExecutor)

    assertThatCode {
      helper.setMediaInView(mock(), mock())
    }.doesNotThrowAnyException()
  }

}