package com.criteo.publisher.tasks;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.CriteoListenerCode;

public class CriteoInterstitialListenerCallTask implements Runnable {

  private static final String TAG = "Criteo.ILCT";

  @Nullable
  private final CriteoInterstitialAdListener criteoInterstitialAdListener;

  @NonNull
  private final CriteoListenerCode code;

  public CriteoInterstitialListenerCallTask(
      @Nullable CriteoInterstitialAdListener listener,
      @NonNull CriteoListenerCode code) {
    this.criteoInterstitialAdListener = listener;
    this.code = code;
  }

  @Override
  public void run() {
    try {
      doRun();
    } catch (Throwable tr) {
      Log.e(TAG, "Internal ILCT PostExec error.", tr);
    }
  }

  private void doRun() {
    if (criteoInterstitialAdListener == null) {
      return;
    }

    switch (code) {
      case VALID:
        criteoInterstitialAdListener.onAdReceived();
        break;
      case INVALID:
        criteoInterstitialAdListener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        break;
    }
  }
}
