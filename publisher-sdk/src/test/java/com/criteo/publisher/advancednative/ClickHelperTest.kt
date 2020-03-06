package com.criteo.publisher.advancednative

import com.criteo.publisher.util.DirectMockRunOnUiThreadExecutor
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class ClickHelperTest {

  private lateinit var runOnUiThreadExecutor: DirectMockRunOnUiThreadExecutor

  private lateinit var clickHelper: ClickHelper

  @Before
  fun setUp() {
    runOnUiThreadExecutor = spy(DirectMockRunOnUiThreadExecutor())
    clickHelper = ClickHelper(runOnUiThreadExecutor)
  }

  @Test
  fun notifyUserClick_GivenNull_DoNothing() {
    clickHelper.notifyUserClickAsync(null)

    verifyZeroInteractions(runOnUiThreadExecutor)
  }

  @Test
  fun notifyUserClick_GivenListener_TriggerListenerInUiThread() {
    val listener = mock<CriteoNativeAdListener>() {
      on { onAdClicked() } doAnswer {
        assertThat(runOnUiThreadExecutor.isRunningOnUiThread).isTrue()
        null
      }
    }

    clickHelper.notifyUserClickAsync(listener)

    verify(listener).onAdClicked()
  }

}