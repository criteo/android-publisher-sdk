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

package com.criteo.publisher.advancednative;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.view.View;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.test.activity.DummyActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class VisibilityTrackerTest {

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  private UiHelper uiHelper;

  @Mock
  private VisibilityChecker visibilityChecker;

  private VisibilityTracker tracker;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    uiHelper = new UiHelper(activityRule);
    tracker = new VisibilityTracker(visibilityChecker);
  }

  @Test
  public void watch_GivenDrawnVisibleView_TriggerItsListener() throws Exception {
    View view = uiHelper.createView();
    VisibilityListener listener = mock(VisibilityListener.class);

    when(visibilityChecker.isVisible(view)).thenReturn(true);

    tracker.watch(view, listener);
    uiHelper.drawViews(view);

    verify(listener, atLeastOnce()).onVisible();
  }

  @Test
  public void watch_GivenDrawnNotVisibleView_DoNotTriggerListener() throws Exception {
    View view = uiHelper.createView();
    VisibilityListener listener = mock(VisibilityListener.class);

    when(visibilityChecker.isVisible(view)).thenReturn(false);

    tracker.watch(view, listener);
    uiHelper.drawViews(view);

    verify(listener, never()).onVisible();
  }

  @Test
  public void watch_GivenNotDrawnView_DoNotTriggerListener() throws Exception {
    View view = uiHelper.createView();
    VisibilityListener listener = mock(VisibilityListener.class);

    tracker.watch(view, listener);

    verify(listener, never()).onVisible();
    verifyZeroInteractions(visibilityChecker);
  }

  @Test
  public void watch_GivenTwoDrawnVisibleViews_TriggerBothListeners() throws Exception {
    View view1 = uiHelper.createView();
    View view2 = uiHelper.createView();
    VisibilityListener listener1 = mock(VisibilityListener.class);
    VisibilityListener listener2 = mock(VisibilityListener.class);

    when(visibilityChecker.isVisible(view1)).thenReturn(true);
    when(visibilityChecker.isVisible(view2)).thenReturn(true);

    tracker.watch(view1, listener1);
    tracker.watch(view2, listener2);
    uiHelper.drawViews(view1, view2);

    verify(listener1, atLeastOnce()).onVisible();
    verify(listener2, atLeastOnce()).onVisible();
  }

  @Test
  public void watch_GivenTwoDrawnViews_TriggerListenerOnlyForVisibleView() throws Exception {
    View visibleView = uiHelper.createView();
    View notVisibleView = uiHelper.createView();
    VisibilityListener listener1 = mock(VisibilityListener.class);
    VisibilityListener listener2 = mock(VisibilityListener.class);

    when(visibilityChecker.isVisible(visibleView)).thenReturn(true);
    when(visibilityChecker.isVisible(notVisibleView)).thenReturn(false);

    tracker.watch(visibleView, listener1);
    tracker.watch(notVisibleView, listener2);
    uiHelper.drawViews(visibleView, notVisibleView);

    verify(listener1, atLeastOnce()).onVisible();
    verify(listener2, never()).onVisible();
  }

  @Test
  public void watch_GivenDrawnVisibleReusedViews_TriggerLastListenerOnly() throws Exception {
    View view = uiHelper.createView();
    VisibilityListener listener1 = mock(VisibilityListener.class);
    VisibilityListener listener2 = mock(VisibilityListener.class);

    when(visibilityChecker.isVisible(view)).thenReturn(true);

    tracker.watch(view, listener1);
    tracker.watch(view, listener2);
    uiHelper.drawViews(view);

    verify(listener1, never()).onVisible();
    verify(listener2, atLeastOnce()).onVisible();
  }

}