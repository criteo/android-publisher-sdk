package com.criteo.publisher.privacy.gdpr

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class GdprDataFactoryTest(
        private val gdprApplies: Boolean,
        private val consentGiven: Boolean
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Boolean>> {
            return listOf(
                    arrayOf(true, true),
                    arrayOf(true, false),
                    arrayOf(false, true),
                    arrayOf(false, false)
            )
        }
    }

    @Test
    fun testCreate_WithGdprAppliesTrue_And_ConsentGivenTrue() {
        // When
        val gdprData = GdprData.create(consentGiven, "fake_consent_data", gdprApplies, 1)

        // Then
        assertEquals(gdprApplies, gdprData.gdprApplies())
        assertEquals(consentGiven, gdprData.consentGiven())
        assertEquals("fake_consent_data", gdprData.consentData())
    }

    @Test
    fun testToJSONObject() {
        // Given
        val gdprData = GdprData.create(consentGiven, "fake_consent_data", gdprApplies, 1)

        // When
        val jsonObject = gdprData.toJSONObject()

        // Then
        assertEquals(gdprApplies, jsonObject.optBoolean("gdprApplies"))
        assertEquals(consentGiven, jsonObject.optBoolean("consentGiven"))
        assertEquals("fake_consent_data", jsonObject.optString("consentData"))
        assertEquals(1, jsonObject.optInt("version"))
    }
}
