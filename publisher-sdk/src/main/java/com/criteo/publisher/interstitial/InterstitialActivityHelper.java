package com.criteo.publisher.interstitial;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.criteo.publisher.CriteoInterstitialActivity;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.Util.CriteoResultReceiver;

public class InterstitialActivityHelper {

  public static final String WEB_VIEW_DATA = "webviewdata";
  public static final String RESULT_RECEIVER = "resultreceiver";

  @NonNull
  private final Context context;

  public InterstitialActivityHelper(@NonNull Context context) {
    this.context = context;
  }

  public boolean isAvailable() {
    Intent intent = createIntent();
    ResolveInfo resolvedInfo = context.getPackageManager()
        .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
    return resolvedInfo != null;
  }

  public void openActivity(
      @NonNull String webViewContent,
      @Nullable CriteoInterstitialAdListener listener) {
    if (!isAvailable()) {
      return;
    }

    CriteoResultReceiver criteoResultReceiver = createReceiver(listener);

    Intent intent = createIntent();
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(WEB_VIEW_DATA, webViewContent);
    intent.putExtra(RESULT_RECEIVER, criteoResultReceiver);

    context.startActivity(intent);
  }

  @NonNull
  private Intent createIntent() {
    return new Intent(context, CriteoInterstitialActivity.class);
  }

  @VisibleForTesting
  CriteoResultReceiver createReceiver(@Nullable CriteoInterstitialAdListener listener) {
    return new CriteoResultReceiver(new Handler(Looper.getMainLooper()), listener);
  }

}
