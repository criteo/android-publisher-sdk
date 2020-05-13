package com.criteo.publisher.advancednative

import android.content.ComponentName
import com.criteo.publisher.activity.TopActivityFinder
import com.criteo.publisher.adview.Redirection
import com.criteo.publisher.adview.RedirectionListener
import com.criteo.publisher.concurrent.DirectMockRunOnUiThreadExecutor
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.net.URI

class ClickHelperTest {

  @Mock
  private lateinit var redirection: Redirection

  @Mock
  private lateinit var topActivityFinder: TopActivityFinder

  private lateinit var runOnUiThreadExecutor: DirectMockRunOnUiThreadExecutor

  private lateinit var clickHelper: ClickHelper

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    runOnUiThreadExecutor = spy(DirectMockRunOnUiThreadExecutor())
    clickHelper = ClickHelper(redirection, topActivityFinder, runOnUiThreadExecutor)
  }

  @Test
  fun notifyUserClick_GivenNull_DoNothing() {
    clickHelper.notifyUserClickAsync(null)

    verifyZeroInteractions(runOnUiThreadExecutor)
  }

  @Test
  fun notifyUserClick_GivenListener_TriggerListenerInUiThread() {
    val listener = expectListenerToBeCalledOnUiThread()

    clickHelper.notifyUserClickAsync(listener)

    verify(listener).onAdClicked()
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun notifyUserIsLeavingApplication_GivenNull_DoNothing() {
    clickHelper.notifyUserIsLeavingApplicationAsync(null)

    verifyZeroInteractions(runOnUiThreadExecutor)
  }

  @Test
  fun notifyUserIsLeavingApplication_GivenListener_TriggerListenerInUiThread() {
    val listener = expectListenerToBeCalledOnUiThread()

    clickHelper.notifyUserIsLeavingApplicationAsync(listener)

    verify(listener).onAdLeftApplication()
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun notifyUserIsBackToApplication_GivenNull_DoNothing() {
    clickHelper.notifyUserIsBackToApplicationAsync(null)

    verifyZeroInteractions(runOnUiThreadExecutor)
  }

  @Test
  fun notifyUserIsBackToApplication_GivenListener_TriggerListenerInUiThread() {
    val listener = expectListenerToBeCalledOnUiThread()

    clickHelper.notifyUserIsBackToApplicationAsync(listener)

    verify(listener).onAdClosed()
    runOnUiThreadExecutor.verifyExpectations()
  }

  @Test
  fun redirectUserTo_GivenUriAndListener_DelegateToRedirection() {
    val uri = URI.create("uri://path.com")
    val listener = mock<RedirectionListener>()
    val activityName = mock<ComponentName>()

    topActivityFinder.stub {
      on { topActivityName } doReturn activityName
    }

    clickHelper.redirectUserTo(uri, listener)

    verify(redirection).redirect("uri://path.com", activityName, listener)
  }

  private fun expectListenerToBeCalledOnUiThread(): CriteoNativeAdListener {
    return mock {
      on { onAdClicked() } doAnswer {
        runOnUiThreadExecutor.expectIsRunningInExecutor()
        null
      }

      on { onAdLeftApplication() } doAnswer {
        runOnUiThreadExecutor.expectIsRunningInExecutor()
        null
      }

      on { onAdClosed() } doAnswer {
        runOnUiThreadExecutor.expectIsRunningInExecutor()
        null
      }
    }
  }

}