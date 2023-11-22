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

package com.criteo.publisher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.MainThread
import com.criteo.publisher.BannerLogMessage.onBannerFailedToClose
import com.criteo.publisher.BannerLogMessage.onBannerFailedToExpand
import com.criteo.publisher.BannerLogMessage.onBannerFailedToResize
import com.criteo.publisher.BannerLogMessage.onBannerFailedToSetOrientationProperties
import com.criteo.publisher.advancednative.VisibilityTracker
import com.criteo.publisher.adview.CriteoMraidController
import com.criteo.publisher.adview.MraidActionResult
import com.criteo.publisher.adview.MraidExpandedActivity
import com.criteo.publisher.adview.MraidExpandedActivityListener
import com.criteo.publisher.adview.MraidInteractor
import com.criteo.publisher.adview.MraidMessageHandler
import com.criteo.publisher.adview.MraidOrientation
import com.criteo.publisher.adview.MraidPlacementType
import com.criteo.publisher.adview.MraidResizeActionResult
import com.criteo.publisher.adview.MraidResizeCustomClosePosition
import com.criteo.publisher.adview.MraidState
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor
import com.criteo.publisher.util.DeviceUtil
import com.criteo.publisher.util.ExternalVideoPlayer
import com.criteo.publisher.util.ViewPositionTracker
import com.criteo.publisher.util.doOnNextLayout
import kotlin.math.abs
import kotlin.math.roundToInt

@OpenForTesting
@Suppress("TooManyFunctions", "LongParameterList")
internal class CriteoBannerMraidController(
    private val bannerView: CriteoBannerAdWebView,
    private val runOnUiThreadExecutor: RunOnUiThreadExecutor,
    visibilityTracker: VisibilityTracker,
    mraidInteractor: MraidInteractor,
    mraidMessageHandler: MraidMessageHandler,
    private val deviceUtil: DeviceUtil,
    viewPositionTracker: ViewPositionTracker,
    externalVideoPlayer: ExternalVideoPlayer
) : CriteoMraidController(
    bannerView,
    visibilityTracker,
    mraidInteractor,
    mraidMessageHandler,
    deviceUtil,
    viewPositionTracker,
    externalVideoPlayer
), MraidExpandedActivityListener {

  private val defaultBannerViewLayoutParams: LayoutParams = bannerView.layoutParams
  private val placeholderView by lazy {
    View(bannerView.context).apply {
      id = R.id.adWebViewPlaceholder
    }
  }
  private var resizedRoot: FrameLayout? = null
  private var resizedAdContainer: RelativeLayout? = null
  private var resizedCloseRegion: View? = null
  private var orientationProperties: Pair<Boolean, MraidOrientation> = true to MraidOrientation.NONE
  private val mediator by lazy {
    DependencyProvider.getInstance()
        .provideMraidExpandBannerMediator()
  }

  override fun getPlacementType(): MraidPlacementType = MraidPlacementType.INLINE

  override fun doExpand(
      width: Double,
      height: Double,
      @MainThread onResult: (result: MraidActionResult) -> Unit
  ) {
    runOnUiThreadExecutor.execute {
      when (currentState) {
        MraidState.LOADING -> onResult(
            MraidActionResult.Error(
                "Can't expand in loading state",
                EXPAND_ACTION
            )
        )
        MraidState.DEFAULT, MraidState.RESIZED -> expandFromDefaultOrResizedState(
            width,
            height,
            onResult
        )
        MraidState.EXPANDED -> onResult(MraidActionResult.Error("Ad already expanded", "expand"))
        MraidState.HIDDEN -> onResult(
            MraidActionResult.Error(
                "Can't expand in hidden state",
                EXPAND_ACTION
            )
        )
      }
    }
  }

  override fun doClose(@MainThread onResult: (result: MraidActionResult) -> Unit) {
    runOnUiThreadExecutor.execute {
      when (currentState) {
        MraidState.LOADING -> onResult(
            MraidActionResult.Error(
                "Can't close in loading state",
                CLOSE_ACTION
            )
        )
        MraidState.DEFAULT -> closeFromDefaultState(onResult)
        MraidState.EXPANDED, MraidState.RESIZED -> closeFromExpandedOrResizedState(onResult)
        MraidState.HIDDEN -> onResult(
            MraidActionResult.Error(
                "Can't close in hidden state",
                CLOSE_ACTION
            )
        )
      }
    }
  }

  override fun doResize(
      width: Double,
      height: Double,
      offsetX: Double,
      offsetY: Double,
      customClosePosition: MraidResizeCustomClosePosition,
      allowOffscreen: Boolean,
      onResult: (result: MraidResizeActionResult) -> Unit
  ) {
    runOnUiThreadExecutor.execute {
      when (currentState) {
        MraidState.LOADING -> onResult(
            MraidResizeActionResult.Error(
                "Can't resize in loading state",
                RESIZE_ACTION
            )
        )
        MraidState.DEFAULT, MraidState.RESIZED -> resizeFromDefaultOrResizedState(
            width,
            height,
            offsetX,
            offsetY,
            customClosePosition,
            allowOffscreen,
            onResult
        )
        MraidState.EXPANDED -> MraidActionResult.Error(
            "Can't resize in expanded state",
            RESIZE_ACTION
        )
        MraidState.HIDDEN -> onResult(
            MraidResizeActionResult.Error(
                "Can't resize in hidden state",
                RESIZE_ACTION
            )
        )
      }
    }
  }

  @Suppress("TooGenericExceptionCaught")
  override fun doSetOrientationProperties(
      allowOrientationChange: Boolean,
      forceOrientation: MraidOrientation,
      @MainThread onResult: (result: MraidActionResult) -> Unit
  ) {
    runOnUiThreadExecutor.execute {
      try {
        orientationProperties = allowOrientationChange to forceOrientation
        if (mediator.hasAnyExpandedBanner()) {
          mediator.requestOrientationChange(allowOrientationChange, forceOrientation)
        }
        onResult(MraidActionResult.Success)
      } catch (t: Throwable) {
        logger.log(onBannerFailedToSetOrientationProperties(bannerView.parentContainer, t))
        onResult(
            MraidActionResult.Error(
                "Failed to set orientation properties",
                "setOrientationProperties"
            )
        )
      }
    }
  }

  @Suppress("TooGenericExceptionCaught")
  override fun resetToDefault() {
    try {
      if (currentState == MraidState.RESIZED) {
        removeBannerFromParentAndCleanupResize()
      } else {
        removeBannerFromParent()
      }
      reattachBannerToOriginalContainer()
      reattachBannerToOriginalContainer()
    } catch (t: Throwable) {
      logger.log(onBannerFailedToClose(bannerView.parentContainer, t))
    }
  }

  override fun onBackClicked() {
    onClose()
  }

  @Suppress("TooGenericExceptionCaught")
  private fun expandFromDefaultOrResizedState(
      width: Double,
      height: Double,
      onResult: (result: MraidActionResult) -> Unit
  ) {
    try {
      if (!bannerView.isAttachedToWindow) {
        onResult(MraidActionResult.Error(DETACHED_FROM_WINDOW_MESSAGE, EXPAND_ACTION))
        return
      }

      if (mediator.hasAnyExpandedBanner()) {
        onResult(MraidActionResult.Error("Another banner is already expanded", EXPAND_ACTION))
        return
      }

      val bannerContainer = bannerView.parentContainer
      val context = (bannerContainer.parent as View).context

      if (currentState == MraidState.RESIZED) {
        removeBannerFromParentAndCleanupResize()
      } else {
        replaceBannerWithPlaceholder(bannerContainer)
      }

      val expandedLayout = RelativeLayout(context)
      expandedLayout.id = R.id.adWebViewDialogContainer
      expandedLayout.layoutParams = LayoutParams(
          LayoutParams.MATCH_PARENT,
          LayoutParams.MATCH_PARENT
      )

      val bannerViewLayoutParams = RelativeLayout.LayoutParams(width.toInt(), height.toInt())
          .also { it.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE) }
      expandedLayout.addView(bannerView, bannerViewLayoutParams)

      val closeButton = createCloseButton(width, height, context)
      expandedLayout.addView(closeButton)

      mediator.saveForExpandedActivity(expandedLayout)
      mediator.setExpandedActivityListener(this)
      val intent = Intent(context, MraidExpandedActivity::class.java)
      intent.putExtra(MraidExpandedActivity.ALLOW_ORIENTATION_CHANGE, orientationProperties.first)
      intent.putExtra(MraidExpandedActivity.ORIENTATION, orientationProperties.second.value)
      context.startActivity(intent)

      onResult(MraidActionResult.Success)
    } catch (t: Throwable) {
      logger.log(onBannerFailedToExpand(bannerView.parentContainer, t))
      onResult(MraidActionResult.Error("Banner failed to expand", EXPAND_ACTION))
    }
  }

  @Suppress("TooGenericExceptionCaught")
  private fun closeFromExpandedOrResizedState(onResult: (result: MraidActionResult) -> Unit) {
    try {
      if (!bannerView.isAttachedToWindow) {
        onResult(MraidActionResult.Error(DETACHED_FROM_WINDOW_MESSAGE, CLOSE_ACTION))
        return
      }

      if (currentState == MraidState.EXPANDED) {
        mediator.requestClose()
        removeBannerFromParent()
      } else {
        removeBannerFromParentAndCleanupResize()
      }

      reattachBannerToOriginalContainer()

      onResult(MraidActionResult.Success)
    } catch (t: Throwable) {
      logger.log(onBannerFailedToClose(bannerView.parentContainer, t))
      onResult(MraidActionResult.Error("Banner failed to close", CLOSE_ACTION))
    }
  }

  private fun reattachBannerToOriginalContainer() {
    val bannerContainer = bannerView.parentContainer
    bannerContainer.addView(
        bannerView,
        LayoutParams(placeholderView.width, placeholderView.height)
    )
    bannerContainer.removeView(placeholderView)
    bannerView.doOnNextLayout {
      bannerView.layoutParams = defaultBannerViewLayoutParams
    }
  }

  private fun replaceBannerWithPlaceholder(bannerContainer: CriteoBannerView) {
    bannerContainer.addView(placeholderView, LayoutParams(bannerView.width, bannerView.height))
    bannerContainer.removeView(bannerView)
  }

  private fun removeBannerFromParent() {
    val expandedParent = bannerView.parent as ViewGroup
    expandedParent.removeView(bannerView)
  }

  private fun closeFromDefaultState(onResult: (result: MraidActionResult) -> Unit) {
    onResult(MraidActionResult.Success)
    bannerView.loadUrl("about:blank")
  }

  private fun createCloseButton(
      width: Double,
      height: Double,
      parentContext: Context
  ): CloseButton {
    val closeButton = CloseButton(parentContext)
    val buttonSize = parentContext.resources.getDimensionPixelSize(R.dimen.close_button_size)
    val layoutParams = RelativeLayout.LayoutParams(buttonSize, buttonSize)

    val isWidthBiggerThanParent = width > getAvailableWidthInPixels()
    layoutParams.addRule(
        if (isWidthBiggerThanParent) RelativeLayout.ALIGN_PARENT_END else RelativeLayout.ALIGN_END,
        if (isWidthBiggerThanParent) RelativeLayout.TRUE else bannerView.id
    )

    val isHeightBiggerThanParent = height > getAvailableHeightInPixels()
    layoutParams.addRule(
        if (isHeightBiggerThanParent) RelativeLayout.ALIGN_PARENT_TOP else RelativeLayout.ALIGN_TOP,
        if (isWidthBiggerThanParent) RelativeLayout.TRUE else bannerView.id
    )
    closeButton.layoutParams = layoutParams

    closeButton.setOnClickListener {
      closeButton.setOnClickListener(null)
      onClose()
    }

    return closeButton
  }

  @Suppress("TooGenericExceptionCaught")
  private fun resizeFromDefaultOrResizedState(
      width: Double,
      height: Double,
      offsetX: Double,
      offsetY: Double,
      customClosePosition: MraidResizeCustomClosePosition,
      allowOffscreen: Boolean,
      onResult: (result: MraidResizeActionResult) -> Unit
  ) {
    try {
      if (!bannerView.isAttachedToWindow) {
        onResult(MraidResizeActionResult.Error(DETACHED_FROM_WINDOW_MESSAGE, RESIZE_ACTION))
        return
      }

      var newXInPx = (currentPosition?.first
          ?: 0).let { deviceUtil.dpToPixel(it) } + deviceUtil.dpToPixel(offsetX.roundToInt())
      var newYInPx = (currentPosition?.second
          ?: 0).let { deviceUtil.dpToPixel(it) } + deviceUtil.dpToPixel(offsetY.roundToInt())

      val widthInPx = deviceUtil.dpToPixel(width.roundToInt())
      val heightInPx = deviceUtil.dpToPixel(height.roundToInt())

      // adjust view position to fit on screen
      if (!allowOffscreen) {
        newXInPx = findClosestPositionOnScreen(newXInPx, maxWidthInPx(), widthInPx)
        newYInPx = findClosestPositionOnScreen(newYInPx, maxHeightInPx(), heightInPx)
      }

      if (!isCloseRegionOnScreen(
              newXInPx,
              newYInPx,
              widthInPx,
              heightInPx,
              customClosePosition
          )) {
        onResult(MraidResizeActionResult.Error("Close button will be offscreen", RESIZE_ACTION))
        return
      }

      if (resizedRoot != null) {
        updateResizedPopup(
            widthInPx,
            heightInPx,
            customClosePosition,
            newXInPx,
            newYInPx,
            allowOffscreen
        )
      } else {
        showResizedPopup(
            widthInPx,
            heightInPx,
            customClosePosition,
            newXInPx,
            newYInPx,
            allowOffscreen
        )
      }
      onResult(
          MraidResizeActionResult.Success(
              deviceUtil.pixelToDp(newXInPx),
              deviceUtil.pixelToDp(newYInPx),
              width.roundToInt(),
              height.roundToInt()
          )
      )
    } catch (t: Throwable) {
      logger.log(onBannerFailedToResize(bannerView.parentContainer, t))
      onResult(MraidResizeActionResult.Error("Banner failed to resize", RESIZE_ACTION))
    }
  }

  private fun updateResizedPopup(
      widthInPx: Int,
      heightInPx: Int,
      customClosePosition: MraidResizeCustomClosePosition,
      newXInPx: Int,
      newYInPx: Int,
      allowOffscreen: Boolean
  ) {
    resizedRoot?.let { root ->
      resizedCloseRegion?.layoutParams = getCloseRegionLayoutParams(customClosePosition)
      resizedAdContainer?.layoutParams = getResizedBannerViewLayoutParams(
          newXInPx,
          newYInPx,
          widthInPx,
          heightInPx,
          allowOffscreen
      )
      val layoutParams = (root.layoutParams as WindowManager.LayoutParams).also {
        it.y = calculateWindowY(newYInPx)
        it.x = newXInPx
        it.width = getOnScreenWidth(widthInPx, newXInPx, allowOffscreen)
        it.height = getOnScreenHeight(heightInPx, newYInPx, allowOffscreen)
      }
      val windowManager = root.context.getSystemService(WINDOW_SERVICE) as WindowManager
      windowManager.updateViewLayout(resizedRoot, layoutParams)
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun showResizedPopup(
      widthInPx: Int,
      heightInPx: Int,
      customClosePosition: MraidResizeCustomClosePosition,
      newXInPx: Int,
      newYInPx: Int,
      allowOffscreen: Boolean
  ) {
    val bannerContainer = bannerView.parentContainer
    val context = (bannerContainer.parent as View).context

    replaceBannerWithPlaceholder(bannerContainer)

    val resizedRoot = FrameLayout(context)
    resizedRoot.clipChildren = false
    val resizedAdContainer = RelativeLayout(context)
    resizedAdContainer.addView(
        bannerView,
        RelativeLayout.LayoutParams(widthInPx, heightInPx)
    )
    resizedRoot.addView(
        resizedAdContainer,
        getResizedBannerViewLayoutParams(newXInPx, newYInPx, widthInPx, heightInPx, allowOffscreen)
    )
    addCloseRegion(resizedAdContainer, customClosePosition)

    val params = WindowManager.LayoutParams(
        getOnScreenWidth(widthInPx, newXInPx, allowOffscreen),
        getOnScreenHeight(heightInPx, newYInPx, allowOffscreen),
        WindowManager.LayoutParams.LAST_SUB_WINDOW,
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    ).also {
      it.y = calculateWindowY(newYInPx)
      it.x = newXInPx
      it.gravity = Gravity.TOP or Gravity.LEFT
    }
    getWindowManager().addView(resizedRoot, params)
    this.resizedRoot = resizedRoot
    this.resizedAdContainer = resizedAdContainer
  }

  // window coordinates include status bar even if app does not draws behind it
  // we are always drawing past status bar(event if it is transparent) to not overlap it's gestures
  private fun calculateWindowY(newYInPx: Int) = newYInPx + getTopBarHeight()

  private fun getOnScreenHeight(
      heightInPx: Int,
      newYInPx: Int,
      allowOffscreen: Boolean
  ) = heightInPx - abs(
      when {
        !allowOffscreen -> 0
        newYInPx < 0 -> {
          newYInPx
        }
        newYInPx + heightInPx > maxHeightInPx() -> {
          newYInPx + heightInPx - maxHeightInPx()
        }
        else -> {
          0
        }
      }
  )

  private fun getOnScreenWidth(
      widthInPx: Int,
      newXInPx: Int,
      allowOffscreen: Boolean
  ) = widthInPx - abs(
      when {
        !allowOffscreen -> 0
        newXInPx < 0 -> newXInPx
        newXInPx + widthInPx > maxWidthInPx() -> {
          newXInPx + widthInPx - maxWidthInPx()
        }
        else -> 0
      }
  )

  private fun findClosestPositionOnScreen(position: Int, maxSize: Int, dimensionSize: Int): Int {
    val minPosition = 0
    val maxPosition = maxSize - dimensionSize

    val validPosition = when {
      position < minPosition -> minPosition
      position > maxPosition -> maxPosition
      else -> position
    }

    return validPosition
  }

  private fun getResizedBannerViewLayoutParams(
      x: Int,
      y: Int,
      widthInPx: Int,
      heightInPx: Int,
      allowOffscreen: Boolean
  ): FrameLayout.LayoutParams {
    return FrameLayout.LayoutParams(
        widthInPx,
        heightInPx
    ).also {
      it.gravity = Gravity.CENTER
      val leftMargin = when {
        !allowOffscreen -> 0
        x < 0 -> {
          x
        }
        x + widthInPx > maxWidthInPx() -> {
          x + widthInPx - maxWidthInPx()
        }
        else -> {
          0
        }
      }

      val topMargin = when {
        !allowOffscreen -> 0
        y < 0 -> {
          y
        }
        y + heightInPx > maxHeightInPx() -> {
          y + heightInPx - maxHeightInPx()
        }
        else -> {
          0
        }
      }

      it.setMargins(leftMargin / 2, topMargin / 2, 0, 0)
    }
  }

  private fun addCloseRegion(
      resizedLayout: RelativeLayout,
      customClosePosition: MraidResizeCustomClosePosition
  ) {
    val closeRegion = View(resizedLayout.context)
    closeRegion.id = R.id.adWebViewCloseRegion
    closeRegion.setOnClickListener {
      onClose()
    }

    resizedLayout.addView(
        closeRegion,
        getCloseRegionLayoutParams(customClosePosition)
    )
    resizedCloseRegion = closeRegion
  }

  private fun getCloseRegionLayoutParams(customClosePosition: MraidResizeCustomClosePosition):
      RelativeLayout.LayoutParams {
    val closeRegionInPx = deviceUtil.dpToPixel(CLOSE_REGION_SIZE)

    return RelativeLayout.LayoutParams(closeRegionInPx, closeRegionInPx).also {
      if (customClosePosition == MraidResizeCustomClosePosition.CENTER) {
        it.addRule(RelativeLayout.CENTER_IN_PARENT)
      } else {
        if (customClosePosition.value.startsWith("top")) {
          it.addRule(RelativeLayout.ALIGN_TOP, bannerView.id)
        }
        if (customClosePosition.value.startsWith("bottom")) {
          it.addRule(RelativeLayout.ALIGN_BOTTOM, bannerView.id)
        }
        if (customClosePosition.value.endsWith("left")) {
          it.addRule(RelativeLayout.ALIGN_LEFT, bannerView.id)
        }
        if (customClosePosition.value.endsWith("right")) {
          it.addRule(RelativeLayout.ALIGN_RIGHT, bannerView.id)
        }
        if (customClosePosition.value.endsWith("center")) {
          it.addRule(RelativeLayout.CENTER_HORIZONTAL, bannerView.id)
        }
      }
    }
  }

  private fun isCloseRegionOnScreen(
      x: Int,
      y: Int,
      width: Int,
      height: Int,
      customClosePosition: MraidResizeCustomClosePosition
  ): Boolean {
    var closeX = 0
    var closeY = 0
    val closeRegionSize = deviceUtil.dpToPixel(CLOSE_REGION_SIZE)
    val halfCloseRegionSize = closeRegionSize / 2

    when (customClosePosition) {
      MraidResizeCustomClosePosition.TOP_CENTER -> {
        closeX = x + (width / 2 - halfCloseRegionSize)
        closeY = y
      }
      MraidResizeCustomClosePosition.TOP_RIGHT -> {
        closeX = x + width - closeRegionSize
        closeY = y
      }
      MraidResizeCustomClosePosition.TOP_LEFT -> {
        closeX = x
        closeY = y
      }
      MraidResizeCustomClosePosition.CENTER -> {
        closeX = x + (width / 2 - halfCloseRegionSize)
        closeY = y + (height / 2 - halfCloseRegionSize)
      }
      MraidResizeCustomClosePosition.BOTTOM_CENTER -> {
        closeX = x + (width / 2 - halfCloseRegionSize)
        closeY = y + height - closeRegionSize
      }
      MraidResizeCustomClosePosition.BOTTOM_RIGHT -> {
        closeX = x + width - closeRegionSize
        closeY = y + height - closeRegionSize
      }
      MraidResizeCustomClosePosition.BOTTOM_LEFT -> {
        closeX = x
        closeY = y + height - closeRegionSize
      }
    }

    return closeX >= 0 &&
        closeX <= maxWidthInPx() - closeRegionSize &&
        closeY >= 0 &&
        closeY <= maxHeightInPx() - closeRegionSize
  }

  private fun removeBannerFromParentAndCleanupResize() {
    resizedAdContainer?.removeView(bannerView)
    getWindowManager().removeView(resizedRoot)
    resizedAdContainer = null
    resizedRoot = null
    resizedCloseRegion = null
  }

  private fun getAvailableWidthInPixels() = bannerView.resources.configuration.screenWidthDp * getDensity()
  private fun getAvailableHeightInPixels() = bannerView.resources.configuration.screenHeightDp * getDensity()
  private fun getDensity() = bannerView.resources.displayMetrics.density

  private fun maxWidthInPx() = maxSize?.first?.let { deviceUtil.dpToPixel(it) } ?: 0

  private fun maxHeightInPx() = maxSize?.second?.let { deviceUtil.dpToPixel(it) } ?: 0

  private fun getWindowManager() = bannerView.context.getSystemService(WINDOW_SERVICE) as WindowManager

  private fun getTopBarHeight() = deviceUtil.getTopSystemBarHeight(bannerView.parentContainer)

  private companion object {
    private const val EXPAND_ACTION = "expand"
    private const val CLOSE_ACTION = "close"
    private const val RESIZE_ACTION = "resize"
    private const val DETACHED_FROM_WINDOW_MESSAGE = "View is detached from window"
    private const val CLOSE_REGION_SIZE = 50
  }
}
