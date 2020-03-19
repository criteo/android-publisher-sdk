package com.criteo.pubsdk_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();
  private Calendar calendar = Calendar.getInstance();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.activity_main);
    findViewById(R.id.buttonDfpCall).setOnClickListener((View v) -> {
      Intent intent = new Intent(getApplicationContext(), DfpActivity.class);
      startActivity(intent);
    });
    findViewById(R.id.buttonMediationCall).setOnClickListener((View v) -> {
      Intent intent = new Intent(getApplicationContext(), MediationActivity.class);
      startActivity(intent);
    });
    findViewById(R.id.buttonStandalone).setOnClickListener((View v) -> {
      Intent intent = new Intent(getApplicationContext(), StandaloneActivity.class);
      startActivity(intent);
    });
    findViewById(R.id.buttoninHouse).setOnClickListener((View v) -> {
      Intent intent = new Intent(getApplicationContext(), InHouseActivity.class);
      startActivity(intent);
    });
    findViewById(R.id.buttonMopubCall).setOnClickListener((View v) -> {
      Intent intent = new Intent(getApplicationContext(), MopubActivity.class);
      startActivity(intent);
    });

    findViewById(R.id.buttonMopubMediation).setOnClickListener((View v) -> {
      Intent intent = new Intent(getApplicationContext(), MopubMediationActivity.class);
      startActivity(intent);
    });

    findViewById(R.id.buttonConsentSelector).setOnClickListener((View v) -> {
      Intent intent = new Intent(getApplicationContext(), PrivacyConsentSelectorActivity.class);
      startActivity(intent);
    });

    findViewById(R.id.buttonTcfSelector).setOnClickListener((View v) -> {
      Intent intent = new Intent(getApplicationContext(), GdprActivity.class);
      startActivity(intent);
    });

    Log.d(TAG, "onCreate:" + calendar.getTimeInMillis());

  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.d(TAG, "onStart:" + calendar.getTimeInMillis());
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.d(TAG, "onStop:" + calendar.getTimeInMillis());
  }
}
