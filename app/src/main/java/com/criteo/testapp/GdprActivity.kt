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

package com.criteo.testapp

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class GdprActivity : AppCompatActivity() {

    private lateinit var defaultSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gdpr)

        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // TCF 2
        val consentStringV2 = findViewById<EditText>(R.id.consentStringV2)
        consentStringV2.setText(defaultSharedPreferences.getString(TCF2_CONSENT_STRING, ""))

        val gdprAppliesV2 = findViewById<EditText>(R.id.gdprAppliesV2)
        val gdprApplies = defaultSharedPreferences.getInt(TCF2_GDPR_APPLIES, -1)
        gdprAppliesV2.setText(gdprApplies.toString())

        // TCF 1
        val consentStringV1 = findViewById<EditText>(R.id.consentStringV1)
        consentStringV1.setText(defaultSharedPreferences.getString(TCF1_CONSENT_STRING, ""))

        val gdprAppliesV1 = findViewById<EditText>(R.id.gdprAppliesV1)
        gdprAppliesV1.setText(defaultSharedPreferences.getString(TCF1_GDPR_APPLIES, ""))

        val saveBtn = findViewById<Button>(R.id.saveTcfData)
        saveBtn.setOnClickListener {
            val editor = defaultSharedPreferences.edit()
            editor.putString(TCF2_CONSENT_STRING, consentStringV2.text.toString())
            editor.putInt(TCF2_GDPR_APPLIES, Integer.valueOf(gdprAppliesV2.text.toString()))

            editor.putString(TCF1_CONSENT_STRING, consentStringV1.text.toString())
            editor.putString(TCF1_GDPR_APPLIES, gdprAppliesV1.text.toString())

            editor.apply()
        }
    }
}
