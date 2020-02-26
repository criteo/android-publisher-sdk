package com.criteo.publisher.privacy.gdpr

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class GdprDataFetcherTest(
        private val gdprApplies: String,
        /** interpretation of gdprApplies as Boolean according to expected GdprDataFetcher#fetch logic */
        private val assertGdprApplies: Boolean,
        private val vendorConsents: String,
        /** interpretation of vendorConsents a Boolean according to expected GdprDataFetcher#fetch logic */
        private val assertVendorConsents: Boolean
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                    arrayOf("1", true, "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000001", true),
                    arrayOf("0", false, "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000000", false),
                    arrayOf("0", false, "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000001", true),
                    arrayOf("1", true, "0000000000000010000000000000000000000100000000000000000000000000000000000000000000000000000", false)
            )
        }
    }

    @Test
    fun testFetch() {
        // Given
        val tcfGdprStrategy = mock<TcfGdprStrategy> {
            on { subjectToGdpr } doReturn gdprApplies
            on { consentString } doReturn "fake_consent_string"
            on { vendorConsents } doReturn vendorConsents
        }

        val tcfStrategyResolver = mock<TcfStrategyResolver> {
            on { resolveTcfStrategy() } doReturn tcfGdprStrategy
        }

        val gdprDataFetcher = GdprDataFetcher(tcfStrategyResolver)

        // When
        val gdprData = gdprDataFetcher.fetch()

        // Then
        assertEquals(assertGdprApplies, gdprData!!.gdprApplies())
        assertEquals(assertVendorConsents, gdprData.consentGiven())
        assertEquals("fake_consent_string", gdprData.consentData())
    }
}
