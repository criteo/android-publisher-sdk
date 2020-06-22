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

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.util.PreconditionsUtil;
import java.lang.ref.WeakReference;
import java.util.List;

public class TopActivityFinder {

  @NonNull
  private final Context context;

  @NonNull
  private WeakReference<Activity> topActivityRef = new WeakReference<>(null);

  public TopActivityFinder(@NonNull Context context) {
    this.context = context;
  }

  @Nullable
  public ComponentName getTopActivityName() {
    Activity topActivity = topActivityRef.get();
    if (topActivity != null) {
      return topActivity.getComponentName();
    }

    // Else we fallback on reading running tasks. ActivityManager.getRunningTasks is deprecated
    // since Lollipop, but for backward compatibility, the method still returns information on
    // owned activities, which is what we look for.
    ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
    if (!PreconditionsUtil.isNotNull(am)) {
      return null;
    }

    List<RunningTaskInfo> taskInfo;
    try {
      //noinspection deprecation
      taskInfo = am.getRunningTasks(1);
    } catch (SecurityException e) {
      PreconditionsUtil.throwOrLog(e);
      return null;
    }

    if (taskInfo.isEmpty()) {
      return null;
    }

    // The getRunningTasks may return information about an activity that the host application does
    // not own, but that is safe to share. This is the cases for launchers. So we need to filter on
    // activities that looks like our own ones.
    ComponentName topActivityName = taskInfo.get(0).topActivity;
    if (topActivityName.getPackageName().startsWith(context.getPackageName())) {
      return topActivityName;
    }

    return null;
  }

  public void registerActivityLifecycleFor(@NonNull Application application) {
    application.registerActivityLifecycleCallbacks(new NoOpActivityLifecycleCallbacks() {
      @Override
      public void onActivityResumed(Activity activity) {
        topActivityRef = new WeakReference<>(activity);
      }

      @Override
      public void onActivityPaused(Activity activity) {
        if (activity.equals(topActivityRef.get())) {
          topActivityRef = new WeakReference<>(null);
        }
      }
    });
  }

}
