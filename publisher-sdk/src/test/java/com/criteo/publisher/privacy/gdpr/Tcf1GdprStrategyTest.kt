package com.criteo.publisher.privacy.gdpr

import com.criteo.publisher.Util.SafeSharedPreferences
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
        val safeSharedPreferences = mock<SafeSharedPreferences> {
           on { getString("IABConsent_ConsentString", "") } doReturn "fake_consent_string"
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "fake_subject_to_gdpr"
        }

        // When
        val tcf1Strategy = Tcf1GdprStrategy(safeSharedPreferences)

        // Then
        assertEquals("fake_consent_string", tcf1Strategy.consentString)
        assertEquals("fake_subject_to_gdpr", tcf1Strategy.subjectToGdpr)
    }

    @Test
    fun testIsProvided_True() {
        // Given
        val safeSharedPreferences = mock<SafeSharedPreferences> {
            on { getString("IABConsent_ConsentString", "") } doReturn "fake_consent_string"
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "fake_subject_to_gdpr"
        }

        // When
        val tcf1Strategy = Tcf1GdprStrategy(safeSharedPreferences)

        // Then
        assertTrue { tcf1Strategy.isProvided }
    }

    @Test
    fun testIsProvided_False_ConsentStringNotProvided() {
        // Given
        val safeSharedPreferences = mock<SafeSharedPreferences> {
            on { getString("IABConsent_ConsentString", "") } doReturn ""
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn "fake_subject_to_gdpr"
        }

        // When
        val tcf1Strategy = Tcf1GdprStrategy(safeSharedPreferences)

        // Then
        assertFalse { tcf1Strategy.isProvided }
    }

    @Test
    fun testIsProvided_False_SubjectToGdprNotProvided() {
        // Given
        val safeSharedPreferences = mock<SafeSharedPreferences> {
            on { getString("IABConsent_ConsentString", "") } doReturn "fake_consent_string"
            on { getString("IABConsent_SubjectToGDPR", "") } doReturn ""
        }

        // When
        val tcf1Strategy = Tcf1GdprStrategy(safeSharedPreferences)

        // Then
        assertFalse { tcf1Strategy.isProvided }
    }

    @Test
    fun testVersion() {
        // Given
        val safeSharedPreferences = mock<SafeSharedPreferences>()

        // When
        val tcf1Strategy = Tcf1GdprStrategy(safeSharedPreferences)

        // Then
        assertEquals(1, tcf1Strategy.version)
    }
}
