package com.criteo.publisher.privacy.gdpr

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class GdprDataFactoryTest(private val gdprApplies: Boolean) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Boolean> {
            return listOf(true, false)
        }
    }

    @Test
    fun testToJSONObject() {
        // Given
        val gdprData = GdprData.create("fake_consent_data", gdprApplies, 1)

        // When
        val jsonObject = gdprData.toJSONObject()

        // Then
        assertEquals(gdprApplies, jsonObject.optBoolean("gdprApplies"))
        assertEquals("fake_consent_data", jsonObject.optString("consentData"))
        assertEquals(1, jsonObject.optInt("version"))
    }
}
