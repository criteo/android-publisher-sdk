package com.criteo.publisher.advancednative;

import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import java.util.concurrent.atomic.AtomicInteger;

public class UiHelper {

  /**
   * Timeout duration in ms that represent the time to wait for UI events.
   */
  private static final int UI_TIMEOUT_MS = 500;

  private final ActivityTestRule<? extends Activity> activityRule;

  private AtomicInteger viewIndex = new AtomicInteger(0);

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

  public int getScreenHeightPixels() {
    return getDisplayMetrics().heightPixels;
  }

  @NonNull
  private DisplayMetrics getDisplayMetrics() {
    WindowManager windowManager = (WindowManager) activityRule.getActivity()
        .getSystemService(Context.WINDOW_SERVICE);

    DisplayMetrics metrics = new DisplayMetrics();
    windowManager.getDefaultDisplay().getMetrics(metrics);
    return metrics;
  }

  public void drawViews(View... views) {
    FrameLayout layout = new FrameLayout(activityRule.getActivity());
    for (View view : views) {
      layout.addView(view);
    }

    runOnMainThreadAndWait(() -> {
      activityRule.getActivity().setContentView(layout);
    });

    waitForUiReady();
  }

  private void waitForUiReady() {
    try {
      Thread.sleep(UI_TIMEOUT_MS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
