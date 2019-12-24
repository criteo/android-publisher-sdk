package com.criteo.pubsdk_android;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
    edit.commit();
  }
}
