package com.criteo.publisher.advancednative;

import android.content.ComponentName;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    runOnUiThreadExecutor.executeAsync(new Runnable() {
      @Override
      public void run() {
        listener.onAdClicked();
      }
    });
  }

  void notifyUserIsLeavingApplicationAsync(@Nullable CriteoNativeAdListener listener) {
    if (listener == null) {
      return;
    }

    runOnUiThreadExecutor.executeAsync(new Runnable() {
      @Override
      public void run() {
        listener.onAdLeftApplication();
      }
    });
  }

  void notifyUserIsBackToApplicationAsync(@Nullable CriteoNativeAdListener listener) {
    if (listener == null) {
      return;
    }

    runOnUiThreadExecutor.executeAsync(new Runnable() {
      @Override
      public void run() {
        listener.onAdClosed();
      }
    });
  }

  void redirectUserTo(@NonNull URI uri, @NonNull RedirectionListener listener) {
    // We are here because a user clicked on ad. So we assume that there is currently an activity
    // running which we use as the leaving activity for the redirection.
    ComponentName hostActivityName = topActivityFinder.getTopActivityName();
    redirection.redirect(uri.toString(), hostActivityName, listener);
  }

}
