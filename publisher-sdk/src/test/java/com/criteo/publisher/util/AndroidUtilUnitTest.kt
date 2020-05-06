package com.criteo.publisher.util

import android.content.Context
import android.util.DisplayMetrics
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class AndroidUtilUnitTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private lateinit var context: Context

  private lateinit var androidUtil: AndroidUtil

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    androidUtil = AndroidUtil(context)
  }

  @Test
  fun dpToPixel_GivenIntegerDensity_ReturnsScaledValue() {
    givenDensity(2.0f)

    assertThat(androidUtil.dpToPixel(1)).isEqualTo(2)
    assertThat(androidUtil.dpToPixel(42)).isEqualTo(84)
  }

  @Test
  fun dpToPixel_GivenFloatDensity_ReturnsScaledCeilValue() {
    givenDensity(31.82f)

    assertThat(androidUtil.dpToPixel(1)).isEqualTo(32)
    assertThat(androidUtil.dpToPixel(42)).isEqualTo(1337)
  }

  private fun givenDensity(density: Float) {
    val metrics = DisplayMetrics()
    metrics.density = density
    context.resources.stub {
      on { displayMetrics } doReturn metrics
    }
  }

}