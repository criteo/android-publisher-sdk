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

package com.criteo.testapp;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.criteo.publisher.Criteo;

public class PrivacyConsentSelectorActivity extends AppCompatActivity {

  private static final String TAG = PrivacyConsentSelectorActivity.class.getSimpleName();

  private EditText ccpaIabEditText;
  private CheckBox ccpaBinaryValue;
  private Button saveBtn;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_privacy_consent_selector);

    ccpaIabEditText = findViewById(R.id.editCcpaIabString);
    ccpaIabEditText.setText(getIabConsentString());

    ccpaBinaryValue = findViewById(R.id.checkboxCcpaBinary);
    ccpaBinaryValue.setChecked(getBinaryValue());

    saveBtn = findViewById(R.id.btnSavePrivacyConsentValues);

    saveBtn.setOnClickListener(v -> {
      saveIabConsentString();
      setBinaryValue();
    });
  }

  private void setBinaryValue() {
    Criteo criteo = Criteo.getInstance();
    criteo.setUsPrivacyOptOut(ccpaBinaryValue.isChecked());
  }

  private boolean getBinaryValue() {
    SharedPreferences defaultSharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(this);

    String binaryValue = defaultSharedPreferences.getString("USPrivacy_Optout", "");
    return Boolean.parseBoolean(binaryValue);
  }

  private String getIabConsentString() {
    SharedPreferences defaultSharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(this);

    return defaultSharedPreferences.getString("IABUSPrivacy_String", "");
  }

  private void saveIabConsentString() {
    Log.d(TAG, "Storing IAB String: " + ccpaIabEditText.getText().toString());

    SharedPreferences defaultSharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(this);

    Editor edit = defaultSharedPreferences.edit();
    edit.putString("IABUSPrivacy_String", ccpaIabEditText.getText().toString());
    edit.apply();
  }
}
