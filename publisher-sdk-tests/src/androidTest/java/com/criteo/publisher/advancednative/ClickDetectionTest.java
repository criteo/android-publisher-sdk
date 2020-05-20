package com.criteo.publisher.advancednative;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.view.ViewGroup;
import com.criteo.publisher.test.activity.DummyActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ClickDetectionTest {

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  private UiHelper uiHelper;

  @Mock
  private NativeViewClickHandler listener;

  private ClickDetection clickDetection;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    uiHelper = new UiHelper(activityRule);
    clickDetection = new ClickDetection();
  }

  @Test
  public void watch_GivenViewAndViewIsClickedManyTimes_NotifyListenerManyTimes() throws Exception {
    View view = uiHelper.createView();

    clickDetection.watch(view, listener);
    view.performClick();
    view.performClick();
    view.performClick();

    verify(listener, times(3)).onClick();
  }

  @Test
  public void watch_GivenViewHierarchyAndViewChildrenAreClickedManyTimes_NotifyListenerManyTimes() throws Exception {
    ViewGroup grandParent = uiHelper.createFrameLayout();
    View sibling = uiHelper.createView();
    ViewGroup parent = uiHelper.createFrameLayout();
    View child1 = uiHelper.createView();
    ViewGroup child2 = uiHelper.createFrameLayout();
    View grandChild = uiHelper.createView();

    grandParent.addView(parent);
    grandParent.addView(sibling);
    parent.addView(child1);
    parent.addView(child2);
    child2.addView(grandChild);

    clickDetection.watch(parent, listener);
    grandParent.performClick();
    sibling.performClick();
    parent.performClick();
    child1.performClick();
    child2.performClick();
    grandChild.performClick();

    verify(listener, times(4)).onClick();
  }

  @Test
  public void watch_GivenAWatchedView_NotifyOnlyLastListener() throws Exception {
    View view = uiHelper.createView();
    NativeViewClickHandler otherListener = mock(NativeViewClickHandler.class);

    clickDetection.watch(view, otherListener);
    clickDetection.watch(view, listener);
    view.performClick();

    verify(otherListener, never()).onClick();
    verify(listener).onClick();
  }

}