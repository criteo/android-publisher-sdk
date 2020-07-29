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

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import androidx.test.rule.ActivityTestRule;
import com.criteo.publisher.test.activity.DummyActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class VisibilityCheckerTest {

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  private UiHelper uiHelper;

  private VisibilityChecker checker;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    uiHelper = new UiHelper(activityRule);
    checker = new VisibilityChecker();
  }

  @Test
  public void isVisible_GivenNotAttachedView_ReturnFalse() throws Exception {
    View view = uiHelper.createView();

    boolean visible = checker.isVisible(view);

    assertFalse(visible);
  }

  @Test
  public void isVisible_GivenVisibleViewOnScreen_ReturnTrue() throws Exception {
    View view = uiHelper.createView();
    uiHelper.drawViews(view);

    boolean visible = checker.isVisible(view);

    assertTrue(visible);
  }

  @Test
  public void isVisible_GivenInvisibleViewOnScreen_ReturnFalse() throws Exception {
    View view = uiHelper.createView();

    ViewGroup parent = uiHelper.createFrameLayout();
    parent.addView(view);
    parent.setVisibility(View.INVISIBLE);

    uiHelper.drawViews(parent);

    boolean visible = checker.isVisible(view);

    assertFalse(visible);
  }

  @Test
  public void isVisible_GivenGoneViewOnScreen_ReturnFalse() throws Exception {
    View view = uiHelper.createView();

    ViewGroup parent = uiHelper.createFrameLayout();
    parent.addView(view);
    parent.setVisibility(View.GONE);

    uiHelper.drawViews(parent);

    boolean visible = checker.isVisible(view);

    assertFalse(visible);
  }

  @Test
  public void isVisible_GivenViewInScrollView_ReturnAccordinglyToScrolling() throws Exception {
    int screenHeightPixels = uiHelper.getActivityHeightPixels();
    int viewHeightPixels = 200;

    View viewAtTop = uiHelper.createView();
    View viewAtBottom = uiHelper.createView();
    RelativeLayout layout = new RelativeLayout(activityRule.getActivity());
    ScrollView scrollView = new ScrollView(activityRule.getActivity());

    layout.addView(viewAtTop);
    layout.addView(viewAtBottom);
    scrollView.addView(layout);

    viewAtTop.getLayoutParams().height = viewHeightPixels;

    RelativeLayout.LayoutParams bottomLayoutParams = (RelativeLayout.LayoutParams) viewAtBottom.getLayoutParams();
    bottomLayoutParams.height = viewHeightPixels;
    bottomLayoutParams.addRule(ALIGN_PARENT_BOTTOM);

    layout.getLayoutParams().height = screenHeightPixels + viewHeightPixels;

    uiHelper.drawViews(scrollView);

    // Scrolling is at top
    assertTrue(checker.isVisible(viewAtTop));
    assertFalse(checker.isVisible(viewAtBottom));

    // Scrolling is at bottom
    runOnMainThreadAndWait(() -> {
      scrollView.fullScroll(View.FOCUS_DOWN);
    });

    assertFalse(checker.isVisible(viewAtTop));
    assertTrue(checker.isVisible(viewAtBottom));

    // Scrolling is between
    runOnMainThreadAndWait(() -> {
      scrollView.scrollTo(0, viewHeightPixels / 2);
    });

    assertTrue(checker.isVisible(viewAtTop));
    assertTrue(checker.isVisible(viewAtBottom));
  }

}