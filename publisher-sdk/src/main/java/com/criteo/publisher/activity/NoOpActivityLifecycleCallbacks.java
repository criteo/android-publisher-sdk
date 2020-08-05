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

package com.criteo.publisher.activity;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import androidx.annotation.NonNull;

public class NoOpActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

  @Override
  public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
  }

  @Override
  public void onActivityStarted(@NonNull Activity activity) {
  }

  @Override
  public void onActivityResumed(@NonNull Activity activity) {
  }

  @Override
  public void onActivityPaused(@NonNull Activity activity) {
  }

  @Override
  public void onActivityStopped(@NonNull Activity activity) {
  }

  @Override
  public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
  }

  @Override
  public void onActivityDestroyed(@NonNull Activity activity) {
  }

}
