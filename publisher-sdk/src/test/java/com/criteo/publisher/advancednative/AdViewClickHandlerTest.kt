package com.criteo.publisher.advancednative

import com.criteo.publisher.adview.RedirectionListener
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
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