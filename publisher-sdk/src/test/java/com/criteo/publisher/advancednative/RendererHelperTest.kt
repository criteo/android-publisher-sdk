package com.criteo.publisher.advancednative

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
    val imageView = mock<ImageView>()
    val imageLoader = mock<ImageLoader>()
    val helper = RendererHelper(imageLoader, uiExecutor)

    doAnswer {
      uiExecutor.expectIsRunningInExecutor()
      null
    }.whenever(imageLoader).loadImageInto(any(), any())

    helper.setMediaInView(media, imageView)

    verify(imageLoader).loadImageInto(url, imageView)
    uiExecutor.expectIsRunningInExecutor()
  }

  @Test
  fun setMediaInView_GivenImageLoaderThrowing_CatchTheException() {
    val url = URI.create("http://image.url").toURL()

    val imageLoader = mock<ImageLoader>() {
      doThrow(Exception::class).whenever(mock).loadImageInto(any(), any())
    }

    val helper = RendererHelper(imageLoader, uiExecutor)

    assertThatCode {
      helper.setMediaInView(url, mock())
    }.doesNotThrowAnyException()
  }

}