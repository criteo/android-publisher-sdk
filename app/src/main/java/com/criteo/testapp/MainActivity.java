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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import com.criteo.testapp.integration.IntegrationSelectorActivity;
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
    findViewById(R.id.buttonStandaloneRecyclerView).setOnClickListener((View v) -> {
      Intent intent = new Intent(getApplicationContext(), StandaloneRecyclerViewActivity.class);
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

    findViewById(R.id.buttonAdMobMediation).setOnClickListener((View v) -> {
      Intent intent = new Intent(getApplicationContext(), AdMobMediationActivity.class);
      startActivity(intent);
    });


    findViewById(R.id.buttonAdvancedBidding).setOnClickListener((View v) -> {
      Intent intent = new Intent(getApplicationContext(), ServerBiddingActivity.class);
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

    findViewById(R.id.buttonIntegrationSelector).setOnClickListener((View v) -> {
      Intent intent = new Intent(getApplicationContext(), IntegrationSelectorActivity.class);
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
