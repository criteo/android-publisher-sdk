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

package com.criteo.publisher.advancednative;

import android.content.ComponentName;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.activity.TopActivityFinder;
import com.criteo.publisher.adview.Redirection;
import com.criteo.publisher.adview.RedirectionListener;
import com.criteo.publisher.concurrent.RunOnUiThreadExecutor;
import java.net.URI;

public class ClickHelper {

  @NonNull
  private final Redirection redirection;

  @NonNull
  private final TopActivityFinder topActivityFinder;

  @NonNull
  private final RunOnUiThreadExecutor runOnUiThreadExecutor;

  public ClickHelper(
      @NonNull Redirection redirection,
      @NonNull TopActivityFinder topActivityFinder,
      @NonNull RunOnUiThreadExecutor runOnUiThreadExecutor
  ) {
    this.redirection = redirection;
    this.topActivityFinder = topActivityFinder;
    this.runOnUiThreadExecutor = runOnUiThreadExecutor;
  }

  void notifyUserClickAsync(@Nullable CriteoNativeAdListener listener) {
    if (listener == null) {
      return;
    }

    runOnUiThreadExecutor.executeAsync(new SafeRunnable() {
      @Override
      public void runSafely() {
        listener.onAdClicked();
      }
    });
  }

  void notifyUserIsLeavingApplicationAsync(@Nullable CriteoNativeAdListener listener) {
    if (listener == null) {
      return;
    }

    runOnUiThreadExecutor.executeAsync(new SafeRunnable() {
      @Override
      public void runSafely() {
        listener.onAdLeftApplication();
      }
    });
  }

  void notifyUserIsBackToApplicationAsync(@Nullable CriteoNativeAdListener listener) {
    if (listener == null) {
      return;
    }

    runOnUiThreadExecutor.executeAsync(new SafeRunnable() {
      @Override
      public void runSafely() {
        listener.onAdClosed();
      }
    });
  }

  void redirectUserTo(@NonNull URI uri, @NonNull RedirectionListener listener) {
    // We are here because a user clicked on an Ad. So we assume that there is currently an activity
    // running which we use as the leaving activity for the redirection.
    ComponentName hostActivityName = topActivityFinder.getTopActivityName();
    redirection.redirect(uri.toString(), hostActivityName, listener);
  }

}
