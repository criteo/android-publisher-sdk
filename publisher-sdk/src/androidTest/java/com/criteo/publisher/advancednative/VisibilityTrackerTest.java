package com.criteo.publisher.advancednative;

import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import com.criteo.publisher.test.activity.DummyActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class VisibilityTrackerTest {

  /**
   * Timeout duration in ms that represent the time to wait for UI events.
   */
  private static final int UI_TIMEOUT_MS = 500;

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  @Mock
  private VisibilityChecker visibilityChecker;

  private VisibilityTracker tracker;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    tracker = new VisibilityTracker(visibilityChecker);
  }

  @Test
  public void watch_GivenDrawnVisibleView_TriggerItsListener() throws Exception {
    View view = createView();
    VisibilityListener listener = mock(VisibilityListener.class);

    when(visibilityChecker.isVisible(view)).thenReturn(true);

    tracker.watch(view, listener);
    drawViews(view);

    verify(listener, timeout(UI_TIMEOUT_MS).atLeastOnce()).onVisible();
  }

  @Test
  public void watch_GivenDrawnNotVisibleView_DoNotTriggerListener() throws Exception {
    View view = createView();
    VisibilityListener listener = mock(VisibilityListener.class);

    when(visibilityChecker.isVisible(view)).thenReturn(false);

    tracker.watch(view, listener);
    drawViews(view);

    verify(listener, after(UI_TIMEOUT_MS).never()).onVisible();
  }

  @Test
  public void watch_GivenNotDrawnView_DoNotTriggerListener() throws Exception {
    View view = createView();
    VisibilityListener listener = mock(VisibilityListener.class);

    tracker.watch(view, listener);

    verify(listener, after(UI_TIMEOUT_MS).never()).onVisible();
    verifyZeroInteractions(visibilityChecker);
  }

  @Test
  public void watch_GivenTwoDrawnVisibleViews_TriggerBothListeners() throws Exception {
    View view1 = createView();
    View view2 = createView();
    VisibilityListener listener1 = mock(VisibilityListener.class);
    VisibilityListener listener2 = mock(VisibilityListener.class);

    when(visibilityChecker.isVisible(view1)).thenReturn(true);
    when(visibilityChecker.isVisible(view2)).thenReturn(true);

    tracker.watch(view1, listener1);
    tracker.watch(view2, listener2);
    drawViews(view1, view2);

    verify(listener1, timeout(UI_TIMEOUT_MS).atLeastOnce()).onVisible();
    verify(listener2, timeout(UI_TIMEOUT_MS).atLeastOnce()).onVisible();
  }

  @Test
  public void watch_GivenTwoDrawnViews_TriggerListenerOnlyForVisibleView() throws Exception {
    View visibleView = createView();
    View notVisibleView = createView();
    VisibilityListener listener1 = mock(VisibilityListener.class);
    VisibilityListener listener2 = mock(VisibilityListener.class);

    when(visibilityChecker.isVisible(visibleView)).thenReturn(true);
    when(visibilityChecker.isVisible(notVisibleView)).thenReturn(false);

    tracker.watch(visibleView, listener1);
    tracker.watch(notVisibleView, listener2);
    drawViews(visibleView, notVisibleView);

    verify(listener1, timeout(UI_TIMEOUT_MS).atLeastOnce()).onVisible();
    verify(listener2, after(UI_TIMEOUT_MS).never()).onVisible();
  }

  @Test
  public void watch_GivenDrawnVisibleReusedViews_TriggerLastListenerOnly() throws Exception {
    View view = createView();
    VisibilityListener listener1 = mock(VisibilityListener.class);
    VisibilityListener listener2 = mock(VisibilityListener.class);

    when(visibilityChecker.isVisible(view)).thenReturn(true);

    tracker.watch(view, listener1);
    tracker.watch(view, listener2);
    drawViews(view);

    verify(listener1, after(UI_TIMEOUT_MS).never()).onVisible();
    verify(listener2, timeout(UI_TIMEOUT_MS).atLeastOnce()).onVisible();
  }

  @NonNull
  private View createView() {
    return new Button(activityRule.getActivity());
  }

  private void drawViews(View... views) {
    FrameLayout layout = new FrameLayout(activityRule.getActivity());
    for (View view : views) {
      layout.addView(view);
    }

    runOnMainThreadAndWait(() -> {
      activityRule.getActivity().setContentView(layout);
    });
  }

}