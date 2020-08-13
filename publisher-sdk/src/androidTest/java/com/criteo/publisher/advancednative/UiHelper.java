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

import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;

import android.app.Activity;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.rule.ActivityTestRule;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class UiHelper {

  /**
   * Timeout duration in ms that represent the time to wait for UI events.
   */
  private static final int UI_TIMEOUT_MS = 500;

  private final ActivityTestRule<? extends Activity> activityRule;

  private final AtomicInteger viewIndex = new AtomicInteger(0);

  public UiHelper(ActivityTestRule<? extends Activity> activityRule) {
    this.activityRule = activityRule;
  }

  @NonNull
  public View createView() {
    Button button = new Button(activityRule.getActivity());

    // Add text on view for debugging
    int index = viewIndex.incrementAndGet();
    button.setText("View ID #" + index);

    return button;
  }

  @NonNull
  public FrameLayout createFrameLayout() {
    return new FrameLayout(activityRule.getActivity());
  }

  /**
   * Return the height in pixel of the usable space in this activity.
   * <p>
   * This may be different than the screen size because this height does not count elements such
   * as:
   * <ul>
   *   <li>navigation bar</li>
   *   <li>status bar</li>
   *   <li>action bar</li>
   * </ul>
   */
  public int getActivityHeightPixels() {
    FrameLayout layout = createFrameLayout();
    drawViews(layout);
    return layout.getHeight();
  }

  @NonNull
  private DisplayMetrics getDisplayMetrics() {
    return activityRule.getActivity().getResources().getDisplayMetrics();
  }

  public void drawViews(View... views) {
    View contentView;

    if (views.length > 1 || true) {
      FrameLayout layout = new FrameLayout(activityRule.getActivity());
      for (View view : views) {
        layout.addView(view);
      }
      contentView = layout;
    } else {
      contentView = views[0];
    }

    runOnMainThreadAndWait(() -> {
      activityRule.getActivity().setContentView(contentView);
    });

    waitForUiReady();
  }

  /**
   * Return the view located a the given position (in DP) in given view hierarchy.
   *
   * The coordinates are relative to the given view, So the (0, 0) coordinate is at the top left of
   * the view. And X goes from left to right, Y goes from top to bottom.
   *
   * You can indicate negative coordinates to start from the ends on the negated axis. For instance,
   * (X=-1, Y=0) indicates the top-right corner of the view.
   *
   * @param view hierarchy of views to look into
   * @param xInDp X coordinate in DP relative to view
   * @param yInDp Y coordinate in DP relative to view
   * @return view at given location or <code>null</code> if none are found.
   */
  @Nullable
  public View findViewAt(@NonNull View view, int xInDp, int yInDp) {
    int xInPixel;
    if (xInDp < 0) {
      xInPixel = dpToPixel(xInDp + 1) + view.getWidth();
    } else {
      xInPixel = dpToPixel(xInDp);
    }

    int yInPixel;
    if (yInDp < 0) {
      yInPixel = dpToPixel(yInDp + 1) + view.getHeight();
    } else {
      yInPixel = dpToPixel(yInDp);
    }

    int[] location = new int[2];
    view.getLocationOnScreen(location);
    int xInScreen = xInPixel + location[0];
    int yInScreen = yInPixel + location[1];

    return findViewAtPixels(view, xInScreen, yInScreen);
  }

  @Nullable
  public View findViewAtPixels(@NonNull View view, int x, int y) {
    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;

      SortedSet<View> children = new TreeSet<>(new DisplayOrderComparator(viewGroup));
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        children.add(viewGroup.getChildAt(i));
      }

      for (View child : children) {
        View foundView = findViewAtPixels(child, x, y);
        if (foundView != null && foundView.isShown()) {
          return foundView;
        }
      }
    }

    int[] location = new int[2];
    view.getLocationOnScreen(location);

    Rect rect = new Rect(
        location[0],
        location[1],
        // Right is excluded in contains, we want it included
        location[0] + view.getWidth() + 1,
        // Bottom is excluded in contains, we want it included
        location[1] + view.getHeight() + 1
    );

    if (rect.contains(x, y)) {
      return view;
    }
    return null;
  }

  private int dpToPixel(int dp) {
    return (int) Math.ceil(dp * getDisplayMetrics().density);
  }

  private void waitForUiReady() {
    try {
      Thread.sleep(UI_TIMEOUT_MS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isParent(@NonNull View parent, @Nullable View other) {
    if (other == null || !(parent instanceof ViewGroup)) {
      return false;
    }

    ViewGroup parentGroup = (ViewGroup) parent;
    for (int i = 0; i < parentGroup.getChildCount(); i++) {
      View child = parentGroup.getChildAt(i);
      if (child.equals(other) || isParent(child, other)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Simulate order of display: first greatest Z-index, then lowest index in view group
   */
  private static class DisplayOrderComparator implements Comparator<View> {

    @NonNull
    private final ViewGroup parent;

    private DisplayOrderComparator(@NonNull ViewGroup parent) {
      this.parent = parent;
    }

    @Override
    public int compare(View o1, View o2) {
      int cmp = Float.compare(o1.getZ(), o2.getZ());
      if (cmp != 0) {
        return -cmp;
      }

      return Integer.compare(parent.indexOfChild(o1), parent.indexOfChild(o2));
    }
  }
}
