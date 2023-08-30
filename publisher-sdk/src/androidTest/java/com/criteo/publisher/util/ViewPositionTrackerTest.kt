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

import androidx.test.rule.ActivityTestRule
import com.criteo.publisher.advancednative.UiHelper
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor
import com.criteo.publisher.concurrent.ThreadingUtil
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.test.activity.DummyActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.atMost
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import javax.inject.Inject

class ViewPositionTrackerTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Rule
  @JvmField
  val activityRule = ActivityTestRule(DummyActivity::class.java)

  @Inject
  private lateinit var runOnUiThreadExecutor: RunOnUiThreadExecutor

  @Inject
  private lateinit var deviceUtil: DeviceUtil

  private lateinit var listener: ViewPositionTracker.PositionListener

  private lateinit var uiHelper: UiHelper

  private lateinit var viewPositionTracker: ViewPositionTracker

  @Before
  fun setUp() {
    listener = mock()
    uiHelper = UiHelper(activityRule)
    viewPositionTracker = ViewPositionTracker(runOnUiThreadExecutor, deviceUtil)
  }

  @Test
  fun watch_givenViewIsDrawn_ShouldTriggerOnPositionChange() {
    val view = uiHelper.createView()

    viewPositionTracker.watch(view, listener)
    uiHelper.drawViews(view)

    val outWindowLocation = IntArray(2)
    view.getLocationInWindow(outWindowLocation)

    verify(listener, atLeastOnce()).onPositionChange(
        eq(deviceUtil.pxToDp(outWindowLocation[0])),
        eq(deviceUtil.pxToDp(outWindowLocation[1])),
        eq(deviceUtil.pxToDp(view.width)),
        eq(deviceUtil.pxToDp(view.height))
    )
  }

  @Test
  fun watch_givenTwoViewsAreDrawn_ShouldTriggerTwoListeners() {
    val view1 = uiHelper.createView()
    val view2 = uiHelper.createView()

    val listener1 = mock<ViewPositionTracker.PositionListener>()
    val listener2 = mock<ViewPositionTracker.PositionListener>()

    viewPositionTracker.watch(view1, listener1)
    viewPositionTracker.watch(view2, listener2)

    uiHelper.drawViews(view1, view2)

    verify(listener1, atLeastOnce()).onPositionChange(any(), any(), any(), any())
    verify(listener2, atLeastOnce()).onPositionChange(any(), any(), any(), any())
  }

  @Test
  fun watch_givenViewIsDrawnAndThenLayoutChanges_ShouldTriggerListenerAtLeastTwoTimes() {
    val view = uiHelper.createView()

    val listener = mock<ViewPositionTracker.PositionListener>()

    viewPositionTracker.watch(view, listener)

    uiHelper.drawViews(view)

    ThreadingUtil.runOnMainThreadAndWait {
      view.layoutParams = view.layoutParams.apply {
        width = 123
        height = 321
      }
    }

    verify(listener, atLeast(2)).onPositionChange(any(), any(), any(), any())
  }

  @Test
  fun watch_givenViewIsDrawnAndWaitForTwoSecondsWhileLayoutIsUnchanged_ShouldTriggerListenerAtMostTwoTimes() {
    val view = uiHelper.createView()

    val listener = mock<ViewPositionTracker.PositionListener>()

    viewPositionTracker.watch(view, listener)
    uiHelper.drawViews(view)

    Thread.sleep(2000)

    verify(listener, atMost(2)).onPositionChange(any(), any(), any(), any())
  }

  @Test
  fun watch_givenNewListenerSetForTheSameView_ShouldReplyWithLatestValue() {
    val view = uiHelper.createView()

    val listener1 = mock<ViewPositionTracker.PositionListener>()
    val listener2 = mock<ViewPositionTracker.PositionListener>()

    viewPositionTracker.watch(view, listener1)
    uiHelper.drawViews(view)
    viewPositionTracker.watch(view, listener2)

    mockedDependenciesRule.waitForIdleState()

    verify(listener2, times(1)).onPositionChange(any(), any(), any(), any())
  }
}
