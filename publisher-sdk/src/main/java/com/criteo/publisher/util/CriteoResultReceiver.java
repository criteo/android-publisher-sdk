package com.criteo.publisher.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.CriteoInterstitialAdListener;
import java.lang.ref.Reference;

public class CriteoResultReceiver extends ResultReceiver {

  public static final String INTERSTITIAL_ACTION = "Action";
  public static final int RESULT_CODE_SUCCESSFUL = 100;
  public static final int ACTION_CLOSED = 201;
  public static final int ACTION_LEFT_CLICKED = 202;

  @NonNull
  private final Reference<CriteoInterstitialAdListener> criteoInterstitialAdListenerRef;

  /**
   * Create a new ResultReceive to receive results.  Your {@link #onReceiveResult} method will be
   * called from the thread running
   * <var>handler</var> if given, or from an arbitrary thread if null.
   */
  public CriteoResultReceiver(
      @NonNull Handler handler,
      @NonNull Reference<CriteoInterstitialAdListener> listenerRef
  ) {
    super(handler);
    this.criteoInterstitialAdListenerRef = listenerRef;
  }

  @Override
  protected void onReceiveResult(int resultCode, Bundle resultData) {
    if (resultCode == RESULT_CODE_SUCCESSFUL) {
      int action = resultData.getInt(INTERSTITIAL_ACTION);
      CriteoInterstitialAdListener listener = criteoInterstitialAdListenerRef.get();

      if (listener != null) {
        switch (action) {
          case ACTION_CLOSED:
            listener.onAdClosed();
            break;
          case ACTION_LEFT_CLICKED:
            listener.onAdClicked();
            listener.onAdLeftApplication();
            break;

          default:
            break;
        }
      }
    }
  }
}
