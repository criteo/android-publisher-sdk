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

import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.JsonSerializer
import com.criteo.publisher.util.writeIntoString
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import kotlin.test.assertEquals

class GdprDataFactoryTest {

    @Rule
    @JvmField
    val mockedDependenciesRule = MockedDependenciesRule()

    @Inject
    private lateinit var serializer: JsonSerializer

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

    private fun GdprData.toJSONObject(): JSONObject = JSONObject(serializer.writeIntoString(this))
}
