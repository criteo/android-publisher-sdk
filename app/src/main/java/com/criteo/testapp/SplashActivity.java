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
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

  private static final int SPLASH_TIME = 1500;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    );
    setContentView(R.layout.activity_splash);

    new Handler().postDelayed(() -> {
      Intent intent = new Intent(SplashActivity.this, MainActivity.class);
      startActivity(intent);
    }, SPLASH_TIME);
  }

}
