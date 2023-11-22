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

package com.criteo.publisher.adview

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import com.criteo.publisher.DependencyProvider

class MraidExpandedActivity : Activity(), MraidExpandBannerListener {

  private val mediator by lazy { DependencyProvider.getInstance().provideMraidExpandBannerMediator() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    intent?.extras?.let {
      val allowOrientationChange = it.getBoolean(ALLOW_ORIENTATION_CHANGE, true)
      val orientation = it.getString(ORIENTATION, MraidOrientation.NONE.value).asMraidOrientation()
      setOrientationProperties(allowOrientationChange, orientation)
    }

    mediator.setBannerListener(this)
    setContentView(mediator.getExpandedBannerView())

    window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    window?.setDimAmount(DIM_AMOUNT)
  }

  override fun onBackPressed() {
    mediator.notifyOnBackClicked()
  }

  override fun onDestroy() {
    super.onDestroy()
    mediator.removeBannerListener()
    mediator.clearExpandedBannerView()
  }

  override fun onOrientationRequested(
      allowOrientationChange: Boolean,
      orientation: MraidOrientation
  ) {
    setOrientationProperties(allowOrientationChange, orientation)
  }

  override fun onCloseRequested() {
    finish()
  }

  private fun setOrientationProperties(allowOrientationChange: Boolean, orientation: MraidOrientation) {
    setRequestedOrientation(allowOrientationChange, orientation)
  }

  companion object {
    internal const val ALLOW_ORIENTATION_CHANGE = "allow_orientation_change"
    internal const val ORIENTATION = "orientation"
    internal const val DIM_AMOUNT = 0.8f
  }
}
