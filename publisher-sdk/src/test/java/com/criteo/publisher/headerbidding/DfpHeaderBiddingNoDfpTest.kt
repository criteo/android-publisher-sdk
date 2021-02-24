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

package com.criteo.publisher.headerbidding

import com.criteo.publisher.util.AdUnitType
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Test

class DfpHeaderBiddingNoDfpTest {

  private lateinit var headerBidding: DfpHeaderBidding

  @Before
  fun setUp() {
    assertThatCode {
      Class.forName("com.google.android.gms.ads.admanager.AdManagerAdRequest")
    }.withFailMessage("""
The tests in this file validate that DFP feature is only degraded, but do not throw, if the
dependency is not provided at runtime.
This assertion check this test is ran without DFP provided.
On IntelliJ, this assertion may appear wrong maybe because it takes some shortcut when creating test
class path.
To run those test locally and properly, you should use Gradle. Either via gradle command line or
via IntelliJ delegating test run to Gradle.
""").isInstanceOf(ClassNotFoundException::class.java)

    headerBidding = DfpHeaderBidding(mock(), mock())
  }

  @Test
  fun canHandle_GivenNoDfpDependency_ReturnFalse() {
    val builder = mock<Any>()

    val handle = headerBidding.canHandle(builder)

    assertThat(handle).isFalse()
  }

  @Test
  fun enrichBid_GivenNoDfpDependency_DoNothing() {
    val builder = mock<Any>()

    headerBidding.enrichBid(builder, AdUnitType.CRITEO_BANNER, mock())

    verifyZeroInteractions(builder)
  }
}
