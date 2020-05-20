package com.criteo.publisher.advancednative;

import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ScrollView;
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
    int screenHeightPixels = uiHelper.getScreenHeightPixels();
    int viewHeightPixels = 200;

    View viewAtTop = uiHelper.createView();
    View viewAtBottom = uiHelper.createView();
    FrameLayout layout = uiHelper.createFrameLayout();
    ScrollView scrollView = new ScrollView(activityRule.getActivity());

    layout.addView(viewAtTop);
    layout.addView(viewAtBottom);
    scrollView.addView(layout);

    viewAtTop.getLayoutParams().height = viewHeightPixels;

    LayoutParams bottomLayoutParams = (LayoutParams) viewAtBottom.getLayoutParams();
    bottomLayoutParams.height = viewHeightPixels;
    bottomLayoutParams.topMargin = screenHeightPixels - viewHeightPixels;

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