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
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.util.WeakHashMap

@OpenForTesting
internal class VisibilityTracker(private val visibilityChecker: VisibilityChecker) {
  @GuardedBy("lock")
  private val trackedViews: MutableMap<View, VisibilityTrackingTask> = WeakHashMap()
  private val lock = Any()

  /**
   * Add the given [View] to the set of watched views.
   *
   *
   * As long as this view live, if at one moment it is drawn and [ ][VisibilityChecker.isVisible] on user screen, then the given listener will be
   * invoked.
   *
   *
   * It is safe to call again this method with the same view and listener, and it is also same to
   * call again with the same view and an other listener. For a given view, only the last registered
   * listener will be invoked. Hence, when having recycled view, you do not need to clean it
   * before.
   *
   * @param view     new or recycle view to watch for visibility
   * @param listener listener to trigger once visibility is detected
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
        visibilityChecker
    )
  }

  @VisibleForTesting
  internal class VisibilityTrackingTask(
      private val trackedViewRef: Reference<View>,
      private val visibilityChecker: VisibilityChecker
  ) :
      ViewTreeObserver.OnPreDrawListener {
    @Volatile
    private var listener: VisibilityListener? = null

    init {
      setUpObserver()
    }

    private fun setUpObserver() {
      val view = trackedViewRef.get() ?: return
      val observer = view.viewTreeObserver
      if (observer.isAlive) {
        observer.addOnPreDrawListener(this)
      }
    }

    fun setListener(listener: VisibilityListener?) {
      this.listener = listener
    }

    override fun onPreDraw(): Boolean {
      if (shouldTrigger()) {
        triggerListener()
      }
      return true
    }

    private fun shouldTrigger(): Boolean {
      val trackedView = trackedViewRef.get() ?: return false
      return visibilityChecker.isVisible(trackedView)
    }

    private fun triggerListener() {
      val listener = listener
      listener?.onVisible()
    }
  }
}
