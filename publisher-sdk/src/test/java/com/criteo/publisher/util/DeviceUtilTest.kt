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
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.WindowMetrics
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Answers
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.whenever
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class DeviceUtilTest {

  @Rule
  @JvmField
  var mockitoRule = MockitoJUnit.rule()

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private lateinit var context: Context

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private lateinit var windowManager: WindowManager

  @InjectMocks
  private lateinit var deviceUtil: DeviceUtil

  private lateinit var metrics: DisplayMetrics

  @Before
  fun setUp() {
    metrics = DisplayMetrics()
    whenever(context.resources.displayMetrics).thenReturn(metrics)
    whenever(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowManager)
  }

  @Test
  fun isTablet_GivenDeviceInPortraitAndWidthBelow600dp_ReturnFalse() {
    metrics.density = 1.25f
    metrics.widthPixels = 749 // 600dp is 750pixel
    metrics.heightPixels = 1000

    val isTablet = deviceUtil.isTablet()

    assertThat(isTablet).isFalse
  }

  @Test
  fun isTablet_GivenDeviceInPortraitAndWidthAboveOrEqualTo600dp_ReturnTrue() {
    metrics.density = 1.25f
    metrics.widthPixels = 750 // 600dp is 750pixel
    metrics.heightPixels = 1000

    val isTablet = deviceUtil.isTablet()

    assertThat(isTablet).isTrue
  }

  @Test
  fun isTablet_GivenDeviceInLandscapeAndHeightBelow600dp_ReturnFalse() {
    metrics.density = 1.25f
    metrics.widthPixels = 1000
    metrics.heightPixels = 749 // 600dp is 750pixel

    val isTablet = deviceUtil.isTablet()

    assertThat(isTablet).isFalse
  }

  @Test
  fun isTablet_GivenDeviceInLandscapeAndHeightAboveOrEqualTo600dp_ReturnTrue() {
    metrics.density = 1.25f
    metrics.widthPixels = 1000
    metrics.heightPixels = 750 // 600dp is 750pixel

    val isTablet = deviceUtil.isTablet()

    assertThat(isTablet).isTrue
  }

  @Test
  fun realScreenSize_GivenApiLevel29_ShouldReturnScreenSizeInDp() {
    setSdkIntVersion(29)
    metrics.density = 2f
    val display = windowManager.defaultDisplay
    Mockito.doAnswer { invocation: InvocationOnMock ->
      val args = invocation.arguments
      val point = args[0] as Point
      point.x = 100
      point.y = 100
      null
    }.whenever(display).getRealSize(ArgumentMatchers.any())

    val (width, height) = deviceUtil.getRealSceeenSize()

    assertThat(width).isEqualTo(50)
    assertThat(height).isEqualTo(50)
  }

  @Test
  fun realScreenSize_GivenApiLevel30_ShouldReturnScreenSizeInDp() {
    setSdkIntVersion(30)
    metrics.density = 2f
    val windowMetrics = Mockito.mock(WindowMetrics::class.java, Mockito.RETURNS_DEEP_STUBS)
    val bounds = windowMetrics.bounds
    whenever(bounds.height()).thenReturn(100)
    whenever(bounds.width()).thenReturn(100)
    whenever(windowManager.maximumWindowMetrics).thenReturn(windowMetrics)

    val (width, height) = deviceUtil.getRealSceeenSize()

    assertThat(width).isEqualTo(50)
    assertThat(height).isEqualTo(50)
  }

  private fun setSdkIntVersion(newValue: Int) {
    val sdkIntField = Build.VERSION::class.java.getField("SDK_INT")
    sdkIntField.isAccessible = true
    val modifiersField = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true
    modifiersField.setInt(sdkIntField, sdkIntField.modifiers and Modifier.FINAL.inv())
    sdkIntField[null] = newValue
  }
}
