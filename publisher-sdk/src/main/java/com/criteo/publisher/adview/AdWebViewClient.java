package com.criteo.publisher.adview;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.List;

public class AdWebViewClient extends WebViewClient {

  @NonNull
  private final AdWebViewListener listener;

  public AdWebViewClient(@NonNull AdWebViewListener listener) {
    this.listener = listener;
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
    }

    return true;
  }

}
