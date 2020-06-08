package com.criteo.publisher.advancednative

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.net.URI

class RendererHelperTest {

  private val uiExecutor = DirectMockRunOnUiThreadExecutor()

  @Mock
  private lateinit var imageLoader: ImageLoader

  private lateinit var helper: RendererHelper

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
    helper = RendererHelper(ImageLoaderHolder(imageLoader), uiExecutor)
  }

  @Test
  fun preloadMedia_GivenImageLoader_DelegateToIt() {
    val url = URI.create("http://image.url").toURL()

    helper.preloadMedia(url)

    verify(imageLoader).preload(url)
  }

  @Test
  fun preloadMedia_GivenImageLoaderThrowing_CatchTheException() {
    val url = URI.create("http://image.url").toURL()
    doThrow(Exception::class).whenever(imageLoader).preload(any())

    assertThatCode {
      helper.preloadMedia(url)
    }.doesNotThrowAnyException()
  }

  @Test
  fun setMediaInView_GivenImageLoader_DelegateToIt() {
    val url = URI.create("http://image.url").toURL()
    val media = CriteoMedia.create(url)

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
    doThrow(Exception::class).whenever(imageLoader).loadImageInto(any(), any(), any())

    assertThatCode {
      helper.setMediaInView(mock(), mock())
    }.doesNotThrowAnyException()
  }

}