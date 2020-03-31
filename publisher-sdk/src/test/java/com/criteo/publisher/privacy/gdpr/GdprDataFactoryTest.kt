package com.criteo.publisher.privacy.gdpr

import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals


class GdprDataFactoryTest {

    @Test
    fun testToJSONObject_ConsentGiven_True() {
        // Given
        val gdprData = GdprData.create("fake_consent_data", true, 1)

        // When
        val jsonObject = gdprData.toJSONObject()

        // Then
        assertTrue(jsonObject.optBoolean("gdprApplies"))
        assertVersionAndConsentData(jsonObject)
    }

    @Test
    fun testToJSONObject_ConsentGiven_False() {
        // Given
        val gdprData = GdprData.create("fake_consent_data", false, 1)

        // When
        val jsonObject = gdprData.toJSONObject()

        // Then
        assertFalse(jsonObject.optBoolean("gdprApplies"))
        assertVersionAndConsentData(jsonObject)
    }

    @Test
    fun testToJSONObject_ConsentGiven_Null() {
        // Given
        val gdprData = GdprData.create("fake_consent_data", null, 1)

        // When
        val jsonObject = gdprData.toJSONObject()

        // Then
        assertFalse(jsonObject.has("gdprApplies"))
        assertVersionAndConsentData(jsonObject)
    }

    private fun assertVersionAndConsentData(jsonObject: JSONObject) {
        assertEquals("fake_consent_data", jsonObject.optString("consentData"))
        assertEquals(1, jsonObject.optInt("version"))
    }
}
