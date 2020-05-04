package com.criteo.publisher.adview;

import android.content.ComponentName;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.DependencyProvider;

public class AdWebViewClient extends WebViewClient {

  @NonNull
  private final RedirectionListener listener;

  @Nullable
  private final ComponentName hostActivityName;

  @NonNull
  private final Redirection redirection;

  public AdWebViewClient(
      @NonNull RedirectionListener listener,
      @Nullable ComponentName hostActivityName
  ) {
    this.listener = listener;
    this.hostActivityName = hostActivityName;
    this.redirection = DependencyProvider.getInstance().provideRedirection();
  }

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    redirection.redirect(url, hostActivityName, listener);
    return true;
  }

}
