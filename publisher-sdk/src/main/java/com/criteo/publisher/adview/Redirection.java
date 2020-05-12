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
import com.criteo.publisher.activity.NoOpActivityLifecycleCallbacks;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
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
            new WeakReference<>(listener)
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

    @NonNull
    private Reference<RedirectionListener> listenerRef;

    public BackOnTargetActivityTracker(
        @NonNull Application application,
        @NonNull ComponentName trackedActivity,
        @NonNull Reference<RedirectionListener> listenerRef
    ) {
      this.application = application;
      this.trackedActivity = trackedActivity;
      this.listenerRef = listenerRef;
    }

    @Override
    public void onActivityResumed(Activity activity) {
      if (!trackedActivity.equals(activity.getComponentName())) {
        return;
      }

      RedirectionListener redirectionListener = listenerRef.get();
      if (redirectionListener == null) {
        return;
      }

      redirectionListener.onUserBackFromAd();
      application.unregisterActivityLifecycleCallbacks(this);
    }
  }
}
