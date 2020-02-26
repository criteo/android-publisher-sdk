package com.criteo.publisher.privacy.gdpr

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TcfStrategyResolvedTests {

    @Test
    fun testResolveTcfStrategy_WhenBothConsentStringsAreProvided_Tcf2IsSelected() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn "non_empty"
            on { getString("IABTCF_gdprApplies", "") } doReturn "non_empty"
            on { getString("IABTCF_VendorConsents", "") } doReturn "non_empty"
        }

        val tcfStrategyResolver = TcfStrategyResolver(sharedPreferences)

        // When
        val tcfStrategy = tcfStrategyResolver.resolveTcfStrategy()

        // Then
        assertTrue {
            tcfStrategy is Tcf2GdprStrategy
        }
    }

    @Test
    fun testResolveTcfStrategy_WhenTCF2ConsentStringsIsProvied_Tcf2IsSelected() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn "non_empty"
            on { getString("IABTCF_gdprApplies", "") } doReturn "non_empty"
            on { getString("IABTCF_VendorConsents", "") } doReturn "non_empty"
            on { getString("IABConsent_ConsentString", "") } doReturn ""
            on { getString("IABConsent_ParsedVendorConsents", "") } doReturn "non_empty"
        }

        val tcfStrategyResolver = TcfStrategyResolver(sharedPreferences)

        // When
        val tcfStrategy = tcfStrategyResolver.resolveTcfStrategy()

        // Then
        assertTrue {
            tcfStrategy is Tcf2GdprStrategy
        }
    }

    @Test
    fun testResolveTcfStrategy_WhenTCF1ConsentStringsIsProvied_Tcf1IsSelected() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn ""
            on { getString("IABTCF_gdprApplies", "") } doReturn ""
            on { getString("IABTCF_VendorConsents", "") } doReturn ""
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "non_empty"
            on { getString("IABConsent_ConsentString", "") } doReturn "non_empty"
            on { getString("IABConsent_ParsedVendorConsents", "") } doReturn "non_empty"
        }

        val tcfStrategyResolver = TcfStrategyResolver(sharedPreferences)

        // When
        val tcfStrategy = tcfStrategyResolver.resolveTcfStrategy()

        // Then
        // Then
        assertTrue {
            tcfStrategy is Tcf1GdprStrategy
        }
    }

    @Test
    fun testResolveTcfStrategy_WhenNoConsentStringIsProvided_StrategyIsNull() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn ""
            on { getString("IABTCF_gdprApplies", "") } doReturn ""
            on { getString("IABTCF_VendorConsents", "") } doReturn ""
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn ""
            on { getString("IABConsent_ConsentString", "") } doReturn ""
            on { getString("IABConsent_ParsedVendorConsents", "") } doReturn ""
        }

        val tcfStrategyResolver = TcfStrategyResolver(sharedPreferences)

        // When
        val tcfStrategy = tcfStrategyResolver.resolveTcfStrategy()

        // Then
        assertNull(tcfStrategy)
    }
}
