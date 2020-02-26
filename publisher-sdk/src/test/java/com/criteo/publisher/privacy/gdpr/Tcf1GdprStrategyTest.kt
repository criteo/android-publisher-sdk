package com.criteo.publisher.privacy.gdpr

import android.content.SharedPreferences
import com.criteo.publisher.privacy.gdpr.Tcf1GdprStrategy
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Tcf1GdprStrategyTest {

    @Test
    fun testAllGetters() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
           on { getString("IABConsent_ConsentString", "") } doReturn "fake_consent_string"
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "fake_subject_to_gdpr"
            on { getString("IABConsent_ParsedVendorConsents", "") } doReturn "fake_parsed_vendor_consent"
        }

        // When
        val tcf1Strategy = Tcf1GdprStrategy(sharedPreferences)

        // Then
        assertEquals("fake_consent_string", tcf1Strategy.consentString)
        assertEquals("fake_subject_to_gdpr", tcf1Strategy.subjectToGdpr)
        assertEquals("fake_parsed_vendor_consent", tcf1Strategy.vendorConsents)
    }

    @Test
    fun testIsProvided_True() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABConsent_ConsentString", "") } doReturn "fake_consent_string"
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "fake_subject_to_gdpr"
            on { getString("IABConsent_ParsedVendorConsents", "") } doReturn "fake_parsed_vendor_consent"
        }

        // When
        val tcf1Strategy = Tcf1GdprStrategy(sharedPreferences)

        // Then
        assertTrue { tcf1Strategy.isProvided }
    }

    @Test
    fun testIsProvided_False_ConsentStringNotProvided() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABConsent_ConsentString", "") } doReturn ""
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "fake_subject_to_gdpr"
            on { getString("IABConsent_ParsedVendorConsents", "") } doReturn "fake_parsed_vendor_consent"
        }

        // When
        val tcf1Strategy = Tcf1GdprStrategy(sharedPreferences)

        // Then
        assertFalse { tcf1Strategy.isProvided }
    }

    @Test
    fun testIsProvided_False_SubjectToGdprNotProvided() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABConsent_ConsentString", "") } doReturn "fake_consent_string"
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn ""
            on { getString("IABConsent_ParsedVendorConsents", "") } doReturn "fake_parsed_vendor_consent"
        }

        // When
        val tcf1Strategy = Tcf1GdprStrategy(sharedPreferences)

        // Then
        assertFalse { tcf1Strategy.isProvided }
    }

    @Test
    fun testIsProvided_False_ParsedVendorNotProvided() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABConsent_ConsentString", "") } doReturn "fake_consent_string"
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "fake_subject_to_gdpr"
            on { getString("IABConsent_ParsedVendorConsents", "") } doReturn ""
        }

        // When
        val tcf1Strategy = Tcf1GdprStrategy(sharedPreferences)

        // Then
        assertFalse { tcf1Strategy.isProvided }
    }

    @Test
    fun testVersion() {
        // Given
        val sharedPreferences = mock<SharedPreferences>()

        // When
        val tcf1Strategy = Tcf1GdprStrategy(sharedPreferences)

        // Then
        assertEquals(1, tcf1Strategy.version)
    }
}
