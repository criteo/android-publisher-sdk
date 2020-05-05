package com.criteo.publisher.advancednative

import com.criteo.publisher.adview.RedirectionListener
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import java.lang.ref.SoftReference
import java.net.URI

class AdChoiceClickHandlerTest {

  @Test
  fun onClick_GivenPrivacyUri_RedirectUserAndNotifyListenerIfLeavingOrGoingBack() {
    val listener = mock<CriteoNativeAdListener>()
    val helper = mock<ClickHelper>()
    val uri = URI.create("privacy://criteo.com")
    val handler = AdChoiceClickHandler(
        uri,
        SoftReference(listener),
        helper
    )

    handler.onClick()

    argumentCaptor<RedirectionListener> {
      verify(helper, never()).notifyUserClickAsync(anyOrNull())
      verify(helper).redirectUserTo(eq(uri), capture())

      lastValue.onUserRedirectedToAd()
      lastValue.onUserBackFromAd()

      verify(helper).notifyUserIsLeavingApplicationAsync(listener)
      verify(helper).notifyUserIsBackToApplicationAsync(listener)
    }
  }

}