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

package com.criteo.publisher.util

import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.GuardedBy
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.util.WeakHashMap

@OpenForTesting
internal class ViewPositionTracker(
    private val runOnUiThreadExecutor: RunOnUiThreadExecutor,
    private val deviceUtil: DeviceUtil
) {

  @GuardedBy("lock")
  private val trackedViews: MutableMap<View, PositionTrackingTask> = WeakHashMap()
  private val lock = Any()

  /**
   * Add the given [View] to the set of watched views.
   *
   * As long as this view is alive, tracker will check position(periodically/onDraw/onLayout) and
   * invoke [PositionListener.onPositionChange] if position has changed
   *
   * It is safe to call again this method with the same view and listener, and it is also same to
   * call again with the same view and an other listener. For a given view, only the last registered
   * listener will be invoked. Hence, when having recycled view, you do not need to clean it
   * before.
   *
   */
  fun watch(view: View, listener: PositionListener) {
    synchronized(lock) {
      var trackingTask = trackedViews[view]
      if (trackingTask == null) {
        trackingTask = PositionTrackingTask(WeakReference(view), runOnUiThreadExecutor, deviceUtil)
      }
      trackingTask.setListener(listener)
    }
  }

  @OpenForTesting
  internal class PositionTrackingTask(
      private val trackedViewRef: Reference<View>,
      private val runOnUiThreadExecutor: RunOnUiThreadExecutor,
      private val deviceUtil: DeviceUtil
  ) : ViewTreeObserver.OnGlobalLayoutListener {
    @Volatile
    private var listener: PositionListener? = null

    private var previousPosition: Position? = null

    private val checkPositionRunnable: Runnable = object : Runnable {
      override fun run() {
        checkPosition()
        if (shouldPollView()) {
          runOnUiThreadExecutor.executeAsync(
              this,
              POSITION_POLL_INTERVAL
          )
        }
      }
    }

    init {
      setUpObserver()
    }

    fun setListener(listener: PositionListener) {
      this.listener = listener
      previousPosition?.let { listener.onPositionChange(it.x, it.y, it.width, it.height) }
    }

    override fun onGlobalLayout() {
      invalidatePosition()
    }

    private fun invalidatePosition() {
      runOnUiThreadExecutor.cancel(checkPositionRunnable)
      runOnUiThreadExecutor.execute(checkPositionRunnable)
    }

    private fun setUpObserver() {
      if (shouldPollView()) {
        val observer = trackedViewRef.get()?.viewTreeObserver
        observer?.addOnGlobalLayoutListener(this)
        invalidatePosition()
      }
    }

    private fun shouldPollView(): Boolean {
      val view = trackedViewRef.get()
      return view != null && view.viewTreeObserver.isAlive
    }

    private fun checkPosition() {
      trackedViewRef.get()?.apply {
        val outWindowLocation = IntArray(2)
        this.getLocationInWindow(outWindowLocation)
        val currentPosition = previousPosition

        val newX = deviceUtil.pxToDp(outWindowLocation[0])
        val newY = deviceUtil.pxToDp(outWindowLocation[1])
        val newWidth = deviceUtil.pxToDp(width)
        val newHeight = deviceUtil.pxToDp(height)

        fun onPositionChange() {
          val newPosition = Position(newX, newY, newWidth, newHeight)
          notifyPositionChange(newPosition)
          previousPosition = newPosition
        }

        when {
          currentPosition == null -> {
            onPositionChange()
          }
          newX != currentPosition.x ||
              newY != currentPosition.y ||
              newWidth != currentPosition.width ||
              newHeight != currentPosition.height -> {
            onPositionChange()
          }
        }
      }
    }

    private fun notifyPositionChange(newPosition: Position) {
      listener?.onPositionChange(
          newPosition.x,
          newPosition.y,
          newPosition.width,
          newPosition.height
      )
    }

    private data class Position(var x: Int, var y: Int, var width: Int, var height: Int)

    companion object {
      const val POSITION_POLL_INTERVAL = 200L
    }
  }

  internal interface PositionListener {

    /**
     * All values are in dp
     *
     * @param x distance from left side
     * @param y distance from top
     * @param width horizontal dimension
     * @param height vertical dimension
     */
    fun onPositionChange(x: Int, y: Int, width: Int, height: Int)
  }
}
