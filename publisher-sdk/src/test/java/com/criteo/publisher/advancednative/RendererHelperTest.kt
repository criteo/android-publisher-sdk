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

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URI

class RendererHelperTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  private val uiExecutor = DirectMockRunOnUiThreadExecutor()

  @Mock
  private lateinit var imageLoader: ImageLoader

  private lateinit var helper: RendererHelper

  @Before
  fun setUp() {
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
    uiExecutor.verifyExpectations()
  }

  @Test
  fun setMediaInView_GivenImageLoaderThrowing_CatchTheException() {
    doThrow(Exception::class).whenever(imageLoader).loadImageInto(any(), any(), any())

    assertThatCode {
      helper.setMediaInView(mock(), mock())
    }.doesNotThrowAnyException()
  }
}
