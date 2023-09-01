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

package com.criteo.publisher.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.rule.ActivityTestRule
import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.test.activity.DummyActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

class ExternalVideoPlayerTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Rule
  @JvmField
  val activityRule = ActivityTestRule(DummyActivity::class.java)

  @SpyBean
  private lateinit var context: Context

  @MockBean
  private lateinit var deviceUtil: DeviceUtil

  private lateinit var onErrorListener: (message: String) -> Unit

  private lateinit var externalVideoPlayer: ExternalVideoPlayer

  @Before
  fun setUp() {
    onErrorListener = mock()
    externalVideoPlayer = ExternalVideoPlayer(context, deviceUtil)
  }

  @Test
  fun play_givenNotValidUrl_ShouldCallbackErrorAndNotInteractWithContext() {
    externalVideoPlayer.play("lol", onErrorListener)

    verify(onErrorListener).invoke("Url is not valid")
    verify(context, never()).startActivity(any())
  }

  @Test
  fun play_givenNoAppAvailableToPlayVideo_ShouldCallbackErrorAndNotInteractWithContext() {
    whenever(deviceUtil.canHandleIntent(any())).thenReturn(false)

    externalVideoPlayer.play("https://criteo.com/cat_video.mp4", onErrorListener)

    verify(onErrorListener).invoke("No app available on device to play this video")
    verify(context, never()).startActivity(any())
  }

  @Test
  fun play_givenAppAvailableToPlayVideo_ShouldCreateIntentWithProperDataAndStartActivity() {
    whenever(deviceUtil.canHandleIntent(any())).thenReturn(true)

    externalVideoPlayer.play("https://criteo.com/cat_video.mp4", onErrorListener)

    verifyZeroInteractions(onErrorListener)
    val argumentCaptor = argumentCaptor<Intent>()
    verify(context).startActivity(argumentCaptor.capture())
    val intent = argumentCaptor.lastValue
    assertThat(intent.flags).isEqualTo(Intent.FLAG_ACTIVITY_NEW_TASK)
    assertThat(intent.type).isEqualTo("video/*")
    assertThat(intent.data).isEqualTo(Uri.parse("https://criteo.com/cat_video.mp4"))
  }
}
