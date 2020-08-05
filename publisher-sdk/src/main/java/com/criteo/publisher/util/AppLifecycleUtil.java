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

package com.criteo.publisher.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.criteo.publisher.AppEvents.AppEvents;
import com.criteo.publisher.BidManager;

public class AppLifecycleUtil implements Application.ActivityLifecycleCallbacks {

  private final AppEvents appEvents;
  private final BidManager bidManager;
  private int started;
  private int resumed;
  private boolean transitionPossible;
  private boolean created;

  public AppLifecycleUtil(AppEvents appEvents, BidManager bidmanager) {
    this.appEvents = appEvents;
    this.bidManager = bidmanager;
    started = 0;
    resumed = 0;
    transitionPossible = false;
    created = false;
  }

  @Override
  public void onActivityCreated(@NonNull Activity activity, Bundle bundle) {
    if (!created) {
      created = true;
      appEvents.sendLaunchEvent();
    }
  }

  @Override
  public void onActivityStarted(@NonNull Activity activity) {
    started += 1;
  }

  @Override
  public void onActivityResumed(@NonNull Activity activity) {
    if (resumed == 0 && !transitionPossible) {
      appEvents.sendActiveEvent();
    }
    transitionPossible = false;
    resumed += 1;
  }

  @Override
  public void onActivityPaused(@NonNull Activity activity) {
    transitionPossible = true;
    resumed -= 1;
  }

  @Override
  public void onActivityStopped(@NonNull Activity activity) {
    if (started == 1) {
      // All transitions pause and stop activities
      if (transitionPossible && resumed == 0) {
        appEvents.sendInactiveEvent();
      }
      appEvents.onApplicationStopped();
      bidManager.onApplicationStopped();
    }
    transitionPossible = false;
    started -= 1;
  }

  @Override
  public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    // intentionally left blank
  }

  @Override
  public void onActivityDestroyed(@NonNull Activity activity) {
    // intentionally left blank
  }
}
