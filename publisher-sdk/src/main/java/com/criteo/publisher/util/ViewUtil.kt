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

/**
 * Copied from
 * https://github.com/androidx/androidx/blob/androidx-main/core/core-ktx/src/main/java/androidx/core/view/View.kt
 */
internal inline fun View.doOnNextLayout(crossinline action: (view: View) -> Unit) {
  addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
    override fun onLayoutChange(
        view: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
      view.removeOnLayoutChangeListener(this)
      action(view)
    }
  })
}
