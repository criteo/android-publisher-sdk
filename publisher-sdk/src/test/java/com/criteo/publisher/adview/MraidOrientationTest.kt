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

package com.criteo.publisher.adview

import android.app.Activity
import android.content.pm.ActivityInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class MraidOrientationTest {

  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private lateinit var activity: Activity

  @Test
  fun asMraidOrientation_givenNoneString_shouldReturnNoneEnumValue() {
    assertThat("none".asMraidOrientation()).isEqualTo(MraidOrientation.NONE)
  }

  @Test
  fun asMraidOrientation_givenPortraitString_shouldReturnPortraitEnumValue() {
    assertThat("portrait".asMraidOrientation()).isEqualTo(MraidOrientation.PORTRAIT)
  }

  @Test
  fun asMraidOrientation_givenLandscapeString_shouldReturnLandscapeEnumValue() {
    assertThat("landscape".asMraidOrientation()).isEqualTo(MraidOrientation.LANDSCAPE)
  }

  @Test
  fun asMraidOrientation_givenRandomString_shouldReturnNoneEnumValue() {
    assertThat("random".asMraidOrientation()).isEqualTo(MraidOrientation.NONE)
  }

  @Test
  fun asMraidOrientation_givenNull_shouldReturnNoneEnumValue() {
    assertThat(null.asMraidOrientation()).isEqualTo(MraidOrientation.NONE)
  }

  @Test
  fun setRequestedOrientation_AllowChangeAndLandscape_ShouldSetRequestedOrientationOnActivityObject() {
    activity.setRequestedOrientation(true, MraidOrientation.LANDSCAPE)

    val inOrder = Mockito.inOrder(activity)
    inOrder.verify(activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    inOrder.verify(activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
  }

  @Test
  fun setRequestedOrientation_AllowChangeAndPortrait_ShouldSetRequestedOrientationOnActivityObject() {
    activity.setRequestedOrientation(true, MraidOrientation.PORTRAIT)

    val inOrder = Mockito.inOrder(activity)
    inOrder.verify(activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    inOrder.verify(activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
  }

  @Test
  fun setRequestedOrientation_AllowChangeAndNone_ShouldSetRequestedOrientationOnActivityObject() {
    activity.setRequestedOrientation(true, MraidOrientation.NONE)

    verify(activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
  }

  @Test
  fun setRequestedOrientation_DoNotAllowChangeAndLandscape_ShouldSetRequestedOrientationOnActivityObject() {
    activity.setRequestedOrientation(false, MraidOrientation.LANDSCAPE)

    verify(activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun setRequestedOrientation_DoNotAllowChangeAndPortrait_ShouldSetRequestedOrientationOnActivityObject() {
    activity.setRequestedOrientation(false, MraidOrientation.PORTRAIT)

    verify(activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    verifyNoMoreInteractions(activity)
  }

  @Test
  fun setRequestedOrientation_DoNotAllowChangeAndNone_ShouldSetRequestedOrientationOnActivityObject() {
    activity.setRequestedOrientation(false, MraidOrientation.NONE)

    verify(activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
    verifyNoMoreInteractions(activity)
  }
}
