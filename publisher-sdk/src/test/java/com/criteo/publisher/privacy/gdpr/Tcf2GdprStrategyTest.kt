package com.criteo.publisher.privacy.gdpr

import android.content.SharedPreferences
import com.criteo.publisher.privacy.gdpr.Tcf2GdprStrategy
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Tcf2GdprStrategyTest {
    @Test
    fun testAllGetters() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn "fake_consent_string"
            on { getString("IABTCF_gdprApplies", "") } doReturn "fake_subject_to_gdpr"
            on { getString("IABTCF_VendorConsents", "") } doReturn "fake_parsed_vendor_consent"

        }

        // When
        val tcf2Strategy = Tcf2GdprStrategy(sharedPreferences)

        // Then
        assertEquals("fake_consent_string", tcf2Strategy.consentString)
        assertEquals("fake_subject_to_gdpr", tcf2Strategy.subjectToGdpr)
        assertEquals("fake_parsed_vendor_consent", tcf2Strategy.vendorConsents)
    }


    @Test
    fun testIsProvided_True() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn "fake_consent_string"
            on { getString("IABTCF_gdprApplies", "") } doReturn "fake_subject_to_gdpr"
            on { getString("IABTCF_VendorConsents", "") } doReturn "fake_parsed_vendor_consent"
        }

        // When
        val tcf2Strategy = Tcf2GdprStrategy(sharedPreferences)

        // Then
        assertTrue { tcf2Strategy.isProvided }
    }

    @Test
    fun testIsProvided_False_ConsentStringNotProvided() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn ""
            on { getString("IABTCF_gdprApplies", "") } doReturn "fake_subject_to_gdpr"
            on { getString("IABTCF_VendorConsents", "") } doReturn "fake_parsed_vendor_consent"
        }

        // When
        val tcf2Strategy = Tcf2GdprStrategy(sharedPreferences)

        // Then
        assertFalse { tcf2Strategy.isProvided }
    }

    @Test
    fun testIsProvided_False_SubjectToGdprNotProvided() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn "fake_consent_string"
            on { getString("IABTCF_gdprApplies", "") } doReturn ""
            on { getString("IABTCF_VendorConsents", "") } doReturn "fake_parsed_vendor_consent"
        }

        // When
        val tcf2Strategy = Tcf2GdprStrategy(sharedPreferences)

        // Then
        assertFalse { tcf2Strategy.isProvided }
    }

    @Test
    fun testIsProvided_False_ParsedVendorNotProvided() {
        // Given
        val sharedPreferences = mock<SharedPreferences> {
            on { getString("IABTCF_TCString", "") } doReturn "fake_consent_string"
            on { getString("IABTCF_gdprApplies", "") } doReturn "fake_subject_to_gdpr"
            on { getString("IABTCF_VendorConsents", "") } doReturn ""
        }

        // When
        val tcf2Strategy = Tcf2GdprStrategy(sharedPreferences)

        // Then
        assertFalse { tcf2Strategy.isProvided }
    }

    @Test
    fun testVersion() {
        // Given
        val sharedPreferences = mock<SharedPreferences>()

        // When
        val tcf2Strategy = Tcf2GdprStrategy(sharedPreferences)

        // Then
        assertEquals(2, tcf2Strategy.version)
    }
}
