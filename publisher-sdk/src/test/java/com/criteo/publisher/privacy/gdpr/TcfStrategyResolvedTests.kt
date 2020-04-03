package com.criteo.publisher.privacy.gdpr

import com.criteo.publisher.util.SafeSharedPreferences
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TcfStrategyResolvedTests {

    @Test
    fun testResolveTcfStrategy_WhenOnlyTcf2KeysAreProvided_Tcf2IsSelected() {
        // Given
        val safeSharedPreferences = mock<SafeSharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn "non_empty"
            on { getInt("IABTCF_gdprApplies", -1) } doReturn 0
            on { getString("IABConsent_ConsentString", "") } doReturn ""
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn ""
        }

        val tcfStrategyResolver = TcfStrategyResolver(safeSharedPreferences)

        // When
        val tcfStrategy = tcfStrategyResolver.resolveTcfStrategy()

        // Then
        assertTrue {
            tcfStrategy is Tcf2GdprStrategy
        }
    }

    @Test
    fun testResolveTcfStrategy_WhenTCF2ConsentStringIsProvided_Tcf2IsSelected() {
        // Given
        val safeSharedPreferences = mock<SafeSharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn "non_empty"
            on { getInt("IABTCF_gdprApplies", -1) } doReturn -1
            on { getString("IABConsent_ConsentString", "") } doReturn "non_empty"
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "non_empty"
        }

        val tcfStrategyResolver = TcfStrategyResolver(safeSharedPreferences)

        // When
        val tcfStrategy = tcfStrategyResolver.resolveTcfStrategy()

        // Then
        assertTrue {
            tcfStrategy is Tcf2GdprStrategy
        }
    }

    @Test
    fun testResolveTcfStrategy_WhenTCF2GdprAppliesIsProvided_Tcf2IsSelected() {
        // Given
        val safeSharedPreferences = mock<SafeSharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn ""
            on { getInt("IABTCF_gdprApplies", -1) } doReturn 0
            on { getString("IABConsent_ConsentString", "") } doReturn "non_empty"
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "non_empty"
        }

        val tcfStrategyResolver = TcfStrategyResolver(safeSharedPreferences)

        // When
        val tcfStrategy = tcfStrategyResolver.resolveTcfStrategy()

        // Then
        assertTrue {
            tcfStrategy is Tcf2GdprStrategy
        }
    }

    @Test
    fun testResolveTcfStrategy_WhenOnlyTCF1ConsentStringIsProvided_Tcf1IsSelected() {
        // Given
        val safeSharedPreferences = mock<SafeSharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn ""
            on { getInt("IABTCF_gdprApplies", -1) } doReturn -1
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn ""
            on { getString("IABConsent_ConsentString", "") } doReturn "non_empty"
        }

        val tcfStrategyResolver = TcfStrategyResolver(safeSharedPreferences)

        // When
        val tcfStrategy = tcfStrategyResolver.resolveTcfStrategy()

        // Then
        // Then
        assertTrue {
            tcfStrategy is Tcf1GdprStrategy
        }
    }

    @Test
    fun testResolveTcfStrategy_WhenOnlyTCF1GdprAppliesIsProvided_Tcf1IsSelected() {
        // Given
        val safeSharedPreferences = mock<SafeSharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn ""
            on { getInt("IABTCF_gdprApplies", -1) } doReturn -1
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "non_empty"
            on { getString("IABConsent_ConsentString", "") } doReturn ""
        }

        val tcfStrategyResolver = TcfStrategyResolver(safeSharedPreferences)

        // When
        val tcfStrategy = tcfStrategyResolver.resolveTcfStrategy()

        // Then
        // Then
        assertTrue {
            tcfStrategy is Tcf1GdprStrategy
        }
    }

    @Test
    fun testResolveTcfStrategy_WhenOnlyTCF1KeysAreProvided_Tcf1IsSelected() {
        // Given
        val safeSharedPreferences = mock<SafeSharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn ""
            on { getInt("IABTCF_gdprApplies", -1) } doReturn -1
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "non_empty"
            on { getString("IABConsent_ConsentString", "") } doReturn "non_empty"
        }

        val tcfStrategyResolver = TcfStrategyResolver(safeSharedPreferences)

        // When
        val tcfStrategy = tcfStrategyResolver.resolveTcfStrategy()

        // Then
        // Then
        assertTrue {
            tcfStrategy is Tcf1GdprStrategy
        }
    }

    @Test
    fun testResolveTcfStrategy_WhenTCF1AndTCF2KeysAreAllProvided_Tcf2IsSelected() {
        // Given
        val safeSharedPreferences = mock<SafeSharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn "non_empty"
            on { getInt("IABTCF_gdprApplies", -1) } doReturn 0
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "non_empty"
            on { getString("IABConsent_ConsentString", "") } doReturn "non_empty"
        }

        val tcfStrategyResolver = TcfStrategyResolver(safeSharedPreferences)

        // When
        val tcfStrategy = tcfStrategyResolver.resolveTcfStrategy()

        // Then
        // Then
        assertTrue {
            tcfStrategy is Tcf2GdprStrategy
        }
    }


    @Test
    fun testResolveTcfStrategy_WhenNoConsentStringIsProvided_StrategyIsNull() {
        // Given
        val safeSharedPreferences = mock<SafeSharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn ""
            on { getInt("IABTCF_gdprApplies", -1) } doReturn -1
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn ""
            on { getString("IABConsent_ConsentString", "") } doReturn ""
        }

        val tcfStrategyResolver = TcfStrategyResolver(safeSharedPreferences)

        // When
        val tcfStrategy = tcfStrategyResolver.resolveTcfStrategy()

        // Then
        assertNull(tcfStrategy)
    }
}
