package com.criteo.publisher.interstitial;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.criteo.publisher.CriteoInterstitialActivity;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.Util.CriteoResultReceiver;

public class InterstitialActivityHelper {

  public static final String WEB_VIEW_DATA = "webviewdata";
  public static final String RESULT_RECEIVER = "resultreceiver";

  public void openActivity(
      @NonNull Context context,
      @NonNull String webViewContent,
      @Nullable CriteoInterstitialAdListener listener) {
    CriteoResultReceiver criteoResultReceiver = createReceiver(listener);

    Intent intent = new Intent(context, CriteoInterstitialActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(WEB_VIEW_DATA, webViewContent);
    intent.putExtra(RESULT_RECEIVER, criteoResultReceiver);

    context.startActivity(intent);
  }

  @VisibleForTesting
  CriteoResultReceiver createReceiver(@Nullable CriteoInterstitialAdListener listener) {
    return new CriteoResultReceiver(new Handler(), listener);
  }

}
