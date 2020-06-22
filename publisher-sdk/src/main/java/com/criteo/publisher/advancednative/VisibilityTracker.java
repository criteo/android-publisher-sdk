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

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public class VisibilityTracker {

  @NonNull
  private final VisibilityChecker visibilityChecker;

  @NonNull
  @GuardedBy("lock")
  private final Map<View, VisibilityTrackingTask> trackedViews = new WeakHashMap<>();

  private final Object lock = new Object();

  public VisibilityTracker(@NonNull VisibilityChecker visibilityChecker) {
    this.visibilityChecker = visibilityChecker;
  }

  /**
   * Add the given {@link View} to the set of watched views.
   * <p>
   * As long as this view live, if at one moment it is drawn and {@linkplain
   * VisibilityChecker#isVisible(View) visible} on user screen, then the given listener will be
   * invoked.
   * <p>
   * It is safe to call again this method with the same view and listener, and it is also same to
   * call again with the same view and an other listener. For a given view, only the last registered
   * listener will be invoked. Hence, when having recycled view, you do not need to clean it
   * before.
   *
   * @param view     new or recycle view to watch for visibility
   * @param listener listener to trigger once visibility is detected
   */
  void watch(@NonNull View view, @NonNull VisibilityListener listener) {
    VisibilityTrackingTask trackingTask;

    synchronized (lock) {
      trackingTask = trackedViews.get(view);
      if (trackingTask == null) {
        trackingTask = startTrackingNewView(view);
        trackedViews.put(view, trackingTask);
      }
    }

    trackingTask.setListener(listener);
  }

  @NonNull
  private VisibilityTrackingTask startTrackingNewView(@NonNull View view) {
    return new VisibilityTrackingTask(new WeakReference<>(view), visibilityChecker);
  }

  @VisibleForTesting
  static class VisibilityTrackingTask implements OnPreDrawListener {

    @NonNull
    private final Reference<View> trackedViewRef;

    @NonNull
    private final VisibilityChecker visibilityChecker;

    @Nullable
    private volatile VisibilityListener listener = null;

    VisibilityTrackingTask(@NonNull Reference<View> viewRef, @NonNull VisibilityChecker visibilityChecker) {
      this.trackedViewRef = viewRef;
      this.visibilityChecker = visibilityChecker;

      setUpObserver();
    }

    private void setUpObserver() {
      View view = trackedViewRef.get();
      if (view == null) {
        return;
      }

      ViewTreeObserver observer = view.getViewTreeObserver();
      if (observer.isAlive()) {
        observer.addOnPreDrawListener(this);
      }
    }

    void setListener(@Nullable VisibilityListener listener) {
      this.listener = listener;
    }

    @Override
    public boolean onPreDraw() {
      if (shouldTrigger()) {
        triggerListener();
      }
      return true;
    }

    private boolean shouldTrigger() {
      View trackedView = trackedViewRef.get();
      if (trackedView == null) {
        return false;
      }

      return visibilityChecker.isVisible(trackedView);
    }

    private void triggerListener() {
      VisibilityListener listener = this.listener;

      if (listener != null) {
        listener.onVisible();
      }
    }
  }

}
