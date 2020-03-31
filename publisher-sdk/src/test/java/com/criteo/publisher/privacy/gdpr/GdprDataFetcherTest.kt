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
        private val assertGdprApplies: Boolean
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                    arrayOf("1", true),
                    arrayOf("0", false)
            )
        }
    }

    @Test
    fun testFetch() {
        // Given
        val tcfGdprStrategy = mock<TcfGdprStrategy> {
            on { subjectToGdpr } doReturn gdprApplies
            on { consentString } doReturn "fake_consent_string"
        }

        val tcfStrategyResolver = mock<TcfStrategyResolver> {
            on { resolveTcfStrategy() } doReturn tcfGdprStrategy
        }

        val gdprDataFetcher = GdprDataFetcher(tcfStrategyResolver)

        // When
        val gdprData = gdprDataFetcher.fetch()

        // Then
        assertEquals(assertGdprApplies, gdprData!!.gdprApplies())
        assertEquals("fake_consent_string", gdprData.consentData())
    }
}
