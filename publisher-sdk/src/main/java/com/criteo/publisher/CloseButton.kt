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

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView

internal class CloseButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ImageView(context, attrs) {

  init {
    contentDescription = resources.getString(R.string.close_button)
    scaleType = ScaleType.FIT_XY
    setImageResource(R.drawable.closebtn)
    setBackgroundColor(Color.TRANSPARENT)
  }
}
