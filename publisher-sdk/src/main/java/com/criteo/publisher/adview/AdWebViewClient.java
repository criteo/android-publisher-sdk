package com.criteo.publisher.adview;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.activity.NoOpActivityLifecycleCallbacks;
import java.util.List;

public class AdWebViewClient extends WebViewClient {

  @NonNull
  private final AdWebViewListener listener;

  @Nullable
  private final ComponentName hostActivityName;

  public AdWebViewClient(
      @NonNull AdWebViewListener listener,
      @Nullable ComponentName hostActivityName
  ) {
    this.listener = listener;
    this.hostActivityName = hostActivityName;
  }

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    Context context = view.getContext();
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    // this callback gets called after the user has clicked on the creative. In case of deeplink,
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

    return true;
  }

  private static class BackOnTargetActivityTracker extends NoOpActivityLifecycleCallbacks {

    @NonNull
    private final Application application;

    @NonNull
    private final ComponentName trackedActivity;

    @Nullable
    private AdWebViewListener listener;

    public BackOnTargetActivityTracker(
        @NonNull Application application,
        @NonNull ComponentName trackedActivity,
        @Nullable AdWebViewListener listener
    ) {
      this.application = application;
      this.trackedActivity = trackedActivity;
      this.listener = listener;
    }

    @Override
    public void onActivityResumed(Activity activity) {
      if (!trackedActivity.equals(activity.getComponentName())) {
        return;
      }

      AdWebViewListener listener = this.listener;
      if (listener == null) {
        return;
      }

      listener.onUserBackFromAd();
      application.unregisterActivityLifecycleCallbacks(this);
      this.listener = null;
    }

  }

}
