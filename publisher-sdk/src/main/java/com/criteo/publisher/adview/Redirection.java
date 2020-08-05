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

package com.criteo.publisher.adview;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.activity.NoOpActivityLifecycleCallbacks;
import java.util.List;

public class Redirection {

  @NonNull
  private final Context context;

  public Redirection(@NonNull Context context) {
    this.context = context;
  }

  public void redirect(
      @NonNull String uri,
      @Nullable ComponentName hostActivityName,
      @NonNull RedirectionListener listener
  ) {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    // this gets called after the user has clicked on the creative. In case of deeplink,
    // if the target application is not installed on the device, an ActivityNotFoundException
    // will be thrown. Therefore, an explicit check is made to ensure that there exists at least
    // one package that can handle the intent
    PackageManager packageManager = context.getPackageManager();
    List<ResolveInfo> list = packageManager.queryIntentActivities(
        intent, PackageManager.MATCH_DEFAULT_ONLY);

    if (list.size() > 0) {
      context.startActivity(intent);

      listener.onUserRedirectedToAd();

      if (hostActivityName != null) {
        Application application = (Application) context.getApplicationContext();
        BackOnTargetActivityTracker tracker = new BackOnTargetActivityTracker(
            application,
            hostActivityName,
            listener
        );
        application.registerActivityLifecycleCallbacks(tracker);
      }
    }
  }

  private static class BackOnTargetActivityTracker extends NoOpActivityLifecycleCallbacks {

    @NonNull
    private final Application application;

    @NonNull
    private final ComponentName trackedActivity;

    @Nullable
    private RedirectionListener listener;

    public BackOnTargetActivityTracker(
        @NonNull Application application,
        @NonNull ComponentName trackedActivity,
        @Nullable RedirectionListener listener
    ) {
      this.application = application;
      this.trackedActivity = trackedActivity;
      this.listener = listener;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
      if (!trackedActivity.equals(activity.getComponentName())) {
        return;
      }

      RedirectionListener listener = this.listener;
      if (listener == null) {
        return;
      }

      listener.onUserBackFromAd();
      application.unregisterActivityLifecycleCallbacks(this);
      this.listener = null;
    }

  }

}
