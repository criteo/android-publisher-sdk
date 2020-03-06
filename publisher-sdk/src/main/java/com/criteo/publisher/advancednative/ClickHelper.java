package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.util.RunOnUiThreadExecutor;

public class ClickHelper {

  @NonNull
  private final RunOnUiThreadExecutor runOnUiThreadExecutor;

  public ClickHelper(@NonNull RunOnUiThreadExecutor runOnUiThreadExecutor) {
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

}
