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

package com.criteo.publisher.interstitial;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.CriteoInterstitialActivity;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.activity.TopActivityFinder;
import com.criteo.publisher.util.CriteoResultReceiver;
import java.lang.ref.WeakReference;

public class InterstitialActivityHelper {

  public static final String WEB_VIEW_DATA = "webviewdata";
  public static final String RESULT_RECEIVER = "resultreceiver";
  public static final String CALLING_ACTIVITY = "callingactivity";

  @NonNull
  private final Context context;

  @NonNull
  private final TopActivityFinder topActivityFinder;

  public InterstitialActivityHelper(
      @NonNull Context context,
      @NonNull TopActivityFinder topActivityFinder
  ) {
    this.context = context;
    this.topActivityFinder = topActivityFinder;
  }

  public boolean isAvailable() {
    Intent intent = createIntent();
    ResolveInfo resolvedInfo = context.getPackageManager()
        .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
    if (resolvedInfo == null) {
      return false;
    }

    int identifier = context.getResources().getIdentifier(
        "activity_criteo_interstitial",
        "layout",
        context.getPackageName());

    return identifier != 0;
  }

  public void openActivity(
      @NonNull String webViewContent,
      @Nullable CriteoInterstitialAdListener listener) {
    if (!isAvailable()) {
      return;
    }

    CriteoResultReceiver criteoResultReceiver = createReceiver(listener);
    ComponentName hostActivityName = topActivityFinder.getTopActivityName();

    Intent intent = createIntent();
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(WEB_VIEW_DATA, webViewContent);
    intent.putExtra(RESULT_RECEIVER, criteoResultReceiver);
    intent.putExtra(CALLING_ACTIVITY, hostActivityName);

    context.startActivity(intent);
  }

  @NonNull
  private Intent createIntent() {
    return new Intent(context, CriteoInterstitialActivity.class);
  }

  @VisibleForTesting
  CriteoResultReceiver createReceiver(@Nullable CriteoInterstitialAdListener listener) {
    return new CriteoResultReceiver(new Handler(Looper.getMainLooper()), new WeakReference<>(listener));
  }
}
