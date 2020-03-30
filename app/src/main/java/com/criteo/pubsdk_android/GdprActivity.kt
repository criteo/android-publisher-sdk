package com.criteo.pubsdk_android

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText

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
