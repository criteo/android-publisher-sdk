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

import com.criteo.publisher.adview.RedirectionListener
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.lang.ref.WeakReference
import java.net.URI

class AdViewClickHandlerTest {

  @Test
  fun onClick_GivenHelperAndListener_NotifyListenerForClick() {
    val uri = URI.create("uri://path.com")
    val listener = mock<CriteoNativeAdListener>()
    val helper = mock<ClickHelper>()
    val task = AdViewClickHandler(
        uri,
        WeakReference(listener),
        helper
    )

    task.onClick()

    argumentCaptor<RedirectionListener> {
      verify(helper).notifyUserClickAsync(listener)
      verify(helper).redirectUserTo(eq(uri), capture())

      lastValue.onUserRedirectedToAd()
      lastValue.onUserBackFromAd()

      verify(helper).notifyUserIsLeavingApplicationAsync(listener)
      verify(helper).notifyUserIsBackToApplicationAsync(listener)
    }
  }
}
