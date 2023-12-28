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

import android.view.View
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class MraidExpandBannerMediatorTest {

  @Rule
  @JvmField
  var mockitoRule = MockitoJUnit.rule()

  private lateinit var mediator: MraidExpandBannerMediator

  @Mock
  private lateinit var bannerListener: MraidExpandBannerListener

  @Mock
  private lateinit var expandedActivityListener: MraidExpandedActivityListener

  @Before
  fun setUp() {
    mediator = MraidExpandBannerMediator()
  }

  @Test
  fun hasAnyExpandedBanner_GivenBannerNotSet_ShouldReturnFalse() {
    assertThat(mediator.hasAnyExpandedBanner()).isFalse
  }

  @Test
  fun hasAnyExpandedBanner_GivenBannerIsSet_ShouldReturnTrue() {
    mediator.saveForExpandedActivity(mock())

    assertThat(mediator.hasAnyExpandedBanner()).isTrue
  }

  @Test
  fun getExpandedBannerView_GivenBannerNotSet_ShouldReturnNull() {
    assertThat(mediator.getExpandedBannerView()).isNull()
  }

  @Test
  fun getExpandedBannerView_GivenBannerIsSet_ShouldReturnSetBannerView() {
    val bannerView = mock<View>()
    mediator.saveForExpandedActivity(bannerView)

    assertThat(mediator.getExpandedBannerView()).isEqualTo(bannerView)
  }

  @Test
  fun clearExpandedBannerView_GivenBannerIsNotSet_ShouldNotThrowAndReturnNull() {
    assertThatCode {
      mediator.clearExpandedBannerView()
      assertThat(mediator.getExpandedBannerView()).isEqualTo(null)
    }.doesNotThrowAnyException()
  }

  @Test
  fun clearExpandedBannerView_GivenBannerIsSet_ShouldReturnNull() {
    mediator.saveForExpandedActivity(mock())

    mediator.clearExpandedBannerView()

    assertThat(mediator.getExpandedBannerView()).isEqualTo(null)
  }

  @Test
  fun requestClose_GivenBannerListenerNotSet_ShouldNotThrow() {
    assertThatCode {
      mediator.requestClose()
    }.doesNotThrowAnyException()
  }

  @Test
  fun requestClose_GivenBannerListenerIsSet_ShouldCallbackListener() {
    mediator.setBannerListener(bannerListener)

    mediator.requestClose()

    verify(bannerListener).onCloseRequested()
  }

  @Test
  fun requestOrientationChange_GivenBannerListenerIsNotSet_ShouldNotThrow() {
    assertThatCode {
      mediator.requestOrientationChange(true, MraidOrientation.NONE)
    }.doesNotThrowAnyException()
  }

  @Test
  fun requestOrientationChange_GivenBannerListenerIsSet_ShouldCallbackListener() {
    mediator.setBannerListener(bannerListener)

    mediator.requestOrientationChange(true, MraidOrientation.LANDSCAPE)

    verify(bannerListener).onOrientationRequested(true, MraidOrientation.LANDSCAPE)
  }

  @Test
  fun removeBannerListenerAfterBeingSet_GivenCallRequests_ShouldNotCallbackListener() {
    mediator.setBannerListener(bannerListener)

    mediator.removeBannerListener()
    mediator.requestClose()
    mediator.requestOrientationChange(true, MraidOrientation.PORTRAIT)

    verifyNoInteractions(bannerListener)
  }

  @Test
  fun notifyOnBackClicked_GivenExpandedActivityListenerIsNotSet_ShouldNotThrow() {
    assertThatCode {
      mediator.notifyOnBackClicked()
    }.doesNotThrowAnyException()
  }

  @Test
  fun notifyOnBackClicked_GivenExpandedActivityListenerIsSet_ShouldCallbackListener() {
    mediator.setExpandedActivityListener(expandedActivityListener)

    mediator.notifyOnBackClicked()

    verify(expandedActivityListener).onBackClicked()
  }

  @Test
  fun removeActivityListenerAfterBeingSet_GivenCallNotify_ShouldNotCallbackListener() {
    mediator.setExpandedActivityListener(expandedActivityListener)

    mediator.removeExpandedActivityListener()
    mediator.notifyOnBackClicked()

    verifyNoInteractions(expandedActivityListener)
  }
}
