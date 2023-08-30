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

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.annotation.MainThread
import com.criteo.publisher.BannerLogMessage.onBannerFailedToClose
import com.criteo.publisher.BannerLogMessage.onBannerFailedToExpand
import com.criteo.publisher.advancednative.VisibilityTracker
import com.criteo.publisher.adview.CriteoMraidController
import com.criteo.publisher.adview.MraidActionResult
import com.criteo.publisher.adview.MraidInteractor
import com.criteo.publisher.adview.MraidMessageHandler
import com.criteo.publisher.adview.MraidPlacementType
import com.criteo.publisher.adview.MraidState
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor
import com.criteo.publisher.util.DeviceUtil
import com.criteo.publisher.util.ViewPositionTracker
import com.criteo.publisher.util.doOnNextLayout

@OpenForTesting
@Suppress("TooManyFunctions", "LongParameterList")
internal class CriteoBannerMraidController(
    private val bannerView: CriteoBannerAdWebView,
    private val runOnUiThreadExecutor: RunOnUiThreadExecutor,
    visibilityTracker: VisibilityTracker,
    mraidInteractor: MraidInteractor,
    mraidMessageHandler: MraidMessageHandler,
    deviceUtil: DeviceUtil,
    viewPositionTracker: ViewPositionTracker
) : CriteoMraidController(
    bannerView,
    visibilityTracker,
    mraidInteractor,
    mraidMessageHandler,
    deviceUtil,
    viewPositionTracker
) {

  private val defaultBannerViewLayoutParams: LayoutParams = bannerView.layoutParams
  private var dialog: Dialog? = null
  private val placeholderView by lazy {
    View(bannerView.context).apply {
      id = R.id.adWebViewPlaceholder
    }
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
        MraidState.DEFAULT -> expandFromDefaultState(width, height, onResult)
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
        MraidState.EXPANDED -> closeFromExpandedState(onResult)
        MraidState.HIDDEN -> onResult(
            MraidActionResult.Error(
                "Can't close in hidden state",
                CLOSE_ACTION
            )
        )
      }
    }
  }

  @Suppress("TooGenericExceptionCaught")
  private fun expandFromDefaultState(
      width: Double,
      height: Double,
      onResult: (result: MraidActionResult) -> Unit
  ) {
    try {
      if (!bannerView.isAttachedToWindow) {
        onResult(MraidActionResult.Error("View is detached from window", EXPAND_ACTION))
        return
      }

      val bannerContainer = bannerView.parentContainer
      val context = (bannerContainer.parent as View).context

      bannerContainer.addView(placeholderView, LayoutParams(bannerView.width, bannerView.height))
      bannerContainer.removeView(bannerView)

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

      dialog = ExpandedDialog(context, ::onClose).apply {
        setContentView(expandedLayout)
        show()
      }

      onResult(MraidActionResult.Success)
    } catch (t: Throwable) {
      logger.log(onBannerFailedToExpand(bannerView.parentContainer, t))
      onResult(MraidActionResult.Error("Banner failed to expand", EXPAND_ACTION))
    }
  }

  @Suppress("TooGenericExceptionCaught")
  private fun closeFromExpandedState(onResult: (result: MraidActionResult) -> Unit) {
    try {
      if (!bannerView.isAttachedToWindow) {
        onResult(MraidActionResult.Error("View is detached from window", CLOSE_ACTION))
        return
      }

      removeBannerFromParent()

      val bannerContainer = bannerView.parentContainer
      bannerContainer.addView(
          bannerView,
          LayoutParams(placeholderView.width, placeholderView.height)
      )
      bannerContainer.removeView(placeholderView)
      bannerView.doOnNextLayout {
        bannerView.layoutParams = defaultBannerViewLayoutParams
      }

      dialog?.dismiss()
      onResult(MraidActionResult.Success)
    } catch (t: Throwable) {
      logger.log(onBannerFailedToClose(bannerView.parentContainer, t))
      onResult(MraidActionResult.Error("Banner failed to close", CLOSE_ACTION))
    }
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

  private fun getAvailableWidthInPixels() = bannerView.resources.configuration.screenWidthDp * getDensity()
  private fun getAvailableHeightInPixels() = bannerView.resources.configuration.screenHeightDp * getDensity()
  private fun getDensity() = bannerView.resources.displayMetrics.density

  private class ExpandedDialog(context: Context, val onBackPressedCallback: () -> Unit) : Dialog(
      context,
      android.R.style.Theme_Translucent
  ) {

    init {
      setCancelable(false)
      requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
      window?.setDimAmount(DIM_AMOUNT)
    }

    override fun onBackPressed() {
      onBackPressedCallback()
    }
  }

  private companion object {
    private const val EXPAND_ACTION = "expand"
    private const val CLOSE_ACTION = "close"
    private const val DIM_AMOUNT = 0.8f
  }
}
