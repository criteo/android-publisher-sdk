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

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.UiThread
import com.criteo.publisher.concurrent.AsyncResources
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import java.net.URL

class CriteoImageLoader(
    private val picasso: Picasso,
    private val asyncResources: AsyncResources
) : ImageLoader {

  override fun preload(imageUrl: URL) {
    picasso.load(imageUrl.toString()).fetch()
  }

  @UiThread
  override fun loadImageInto(
      imageUrl: URL,
      imageView: ImageView,
      placeholder: Drawable?
  ) {
    asyncResources.newResource {
      picasso.load(imageUrl.toString())
          .placeholder(placeholder)
          .into(imageView, object : Callback {
            override fun onSuccess() = release()
            override fun onError(e: Exception) = release()
          })
    }
  }

  private fun RequestCreator.placeholder(placeholder: Drawable?): RequestCreator {
    if (placeholder != null) {
      return placeholder(placeholder)
    }
    return this
  }
}
