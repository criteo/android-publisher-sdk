package com.criteo.publisher.advancednative

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import java.lang.ref.WeakReference

class AdViewClickHandlerTest {

  @Test
  fun onClick_GivenHelperAndListener_NotifyListenerForClick() {
    val listener = mock<CriteoNativeAdListener>()
    val helper = mock<ClickHelper>()
    val task = AdViewClickHandler(
        WeakReference(listener),
        helper
    )

    task.onClick()

    verify(helper).notifyUserClickAsync(listener)
  }

}