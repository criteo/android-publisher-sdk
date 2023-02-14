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
package com.criteo.publisher.advancednative

import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.GuardedBy
import androidx.annotation.VisibleForTesting
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.util.WeakHashMap

@OpenForTesting
internal class VisibilityTracker(
    private val visibilityChecker: VisibilityChecker,
    private val runOnUiThreadExecutor: RunOnUiThreadExecutor
) {
  @GuardedBy("lock")
  private val trackedViews: MutableMap<View, VisibilityTrackingTask> = WeakHashMap()
  private val lock = Any()

  /**
   * Add the given [View] to the set of watched views.
   *
   *
   * As long as this view is alive, tracker will check(periodically/onDraw/onLayout) for current
   * visibility state on screen by invoking [VisibilityChecker.isVisible],
   * then the given listener will be invoked with appropriate state.
   *
   *
   * It is safe to call again this method with the same view and listener, and it is also same to
   * call again with the same view and an other listener. For a given view, only the last registered
   * listener will be invoked. Hence, when having recycled view, you do not need to clean it
   * before.
   *
   * @param view     new or recycle view to watch for visibility
   * @param listener listener to trigger on visibility change
   */
  fun watch(view: View, listener: VisibilityListener) {
    var trackingTask: VisibilityTrackingTask?
    synchronized(lock) {
      trackingTask = trackedViews[view]
      if (trackingTask == null) {
        trackingTask = startTrackingNewView(view)
        trackedViews[view] = trackingTask!!
      }
    }
    trackingTask!!.setListener(listener)
  }

  private fun startTrackingNewView(view: View): VisibilityTrackingTask {
    return VisibilityTrackingTask(
        WeakReference(view),
        visibilityChecker,
        runOnUiThreadExecutor
    )
  }

  @VisibleForTesting
  internal class VisibilityTrackingTask(
      private val trackedViewRef: Reference<View>,
      private val visibilityChecker: VisibilityChecker,
      private val runOnUiThreadExecutor: RunOnUiThreadExecutor
  ) : ViewTreeObserver.OnPreDrawListener,
      ViewTreeObserver.OnGlobalLayoutListener {
    @Volatile
    private var listener: VisibilityListener? = null

    private val checkVisibilityRunnable: Runnable = object : Runnable {
      override fun run() {
        checkVisibility()
        if (shouldPollView()) {
          runOnUiThreadExecutor.executeAsync(this, VISIBILITY_POLL_INTERVAL)
        }
      }
    }

    init {
      setUpObserver()
    }

    private fun setUpObserver() {
      if (shouldPollView()) {
        val observer = trackedViewRef.get()?.viewTreeObserver
        observer?.addOnPreDrawListener(this)
        observer?.addOnGlobalLayoutListener(this)
      }
    }

    fun setListener(listener: VisibilityListener?) {
      this.listener = listener
    }

    override fun onPreDraw(): Boolean {
      invalidateVisibility()
      return true
    }

    override fun onGlobalLayout() {
      invalidateVisibility()
    }

    private fun invalidateVisibility() {
      runOnUiThreadExecutor.cancel(checkVisibilityRunnable)
      runOnUiThreadExecutor.execute(checkVisibilityRunnable)
    }

    private fun checkVisibility() {
      trackedViewRef.get()?.apply {
        val isVisible = visibilityChecker.isVisible(this)
        if (isVisible) listener?.onVisible() else listener?.onGone()
      }
    }

    private fun shouldPollView(): Boolean {
      val view = trackedViewRef.get()
      return view != null && view.viewTreeObserver.isAlive
    }

    companion object {
      const val VISIBILITY_POLL_INTERVAL = 200L
    }
  }
}
