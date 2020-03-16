package com.criteo.publisher.headerbidding

import com.criteo.publisher.BidManager
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class DfpHeaderBiddingNoDfpTest {

  @Mock
  private lateinit var bidManager: BidManager

  private lateinit var headerBidding: DfpHeaderBidding

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    assertThatCode {
      Class.forName("com.google.android.gms.ads.doubleclick.PublisherAdRequest")
    }.withFailMessage("""
The tests in this file validate that DFP feature is only degraded, but do not throw, if the
dependency is not provided at runtime.
This assertion check this test is ran without DFP provided.
On IntelliJ, this assertion may appear wrong maybe because it takes some shortcut when creating test
class path.
To run those test locally and properly, you should use Gradle. Either via gradle command line or
via IntelliJ delegating test run to Gradle.
""").isInstanceOf(ClassNotFoundException::class.java)

    headerBidding = DfpHeaderBidding(
        bidManager
    )
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

    headerBidding.enrichBid(builder, mock())

    verifyZeroInteractions(bidManager)
  }

}