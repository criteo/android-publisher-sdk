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

package com.criteo.publisher.privacy.gdpr

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
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
