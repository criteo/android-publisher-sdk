package com.criteo.publisher;

import static com.criteo.publisher.Util.CriteoResultReceiver.ACTION_CLOSED;
import static com.criteo.publisher.Util.CriteoResultReceiver.ACTION_LEFT_CLICKED;
import static com.criteo.publisher.Util.CriteoResultReceiver.INTERSTITIAL_ACTION;
import static com.criteo.publisher.Util.CriteoResultReceiver.RESULT_CODE_SUCCESSFUL;
import static com.criteo.publisher.interstitial.InterstitialActivityHelper.RESULT_RECEIVER;
import static com.criteo.publisher.interstitial.InterstitialActivityHelper.WEB_VIEW_DATA;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import java.util.List;

public class CriteoInterstitialActivity extends Activity {

  private WebView webView;
  private ResultReceiver resultReceiver;
  private ImageButton closeButton;
  private RelativeLayout adLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_criteo_interstitial);
    adLayout = findViewById(R.id.AdLayout);
    webView = findViewById(R.id.webview);
    closeButton = findViewById(R.id.closeButton);

    prepareWebView();

    Bundle bundle = getIntent().getExtras();
    if (bundle != null && bundle.getString(WEB_VIEW_DATA) != null) {
      String webViewData = bundle.getString(WEB_VIEW_DATA);
      resultReceiver = bundle.getParcelable(RESULT_RECEIVER);
      displayWebView(webViewData);
    }

    closeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        close();
      }
    });

  }

  private void close() {
    Bundle bundle = new Bundle();
    bundle.putInt(INTERSTITIAL_ACTION, ACTION_CLOSED);
    resultReceiver.send(RESULT_CODE_SUCCESSFUL, bundle);
    finish();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    adLayout.removeAllViews();
    webView.destroy();
    webView = null;
  }

  private void displayWebView(String webViewData) {
    webView.loadDataWithBaseURL("https://criteo.com", webViewData, "text/html", "UTF-8",
        "about:blank");
  }

  private void prepareWebView() {
    webView.getSettings().setJavaScriptEnabled(true);
    webView.setWebViewClient(new InterstitialWebViewClient());
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  @Override
  public void onBackPressed() {
    close();
  }

  @VisibleForTesting
  WebView getWebView() {
    return webView;
  }

  private class InterstitialWebViewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      // this callback gets called after the user has clicked on the creative. In case of deeplink,
      // if the target application is not installed on the device, an ActivityNotFoundException
      // will be thrown. Therefore, an explicit check is made to ensure that there exists at least
      // one package that can handle the intent
      PackageManager packageManager = getPackageManager();
      List<ResolveInfo> list = packageManager
          .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

      if (list.size() > 0) {
        view.getContext().startActivity(intent);
        Bundle bundle = new Bundle();
        bundle.putInt(INTERSTITIAL_ACTION, ACTION_LEFT_CLICKED);
        resultReceiver.send(RESULT_CODE_SUCCESSFUL, bundle);
        finish();
      }

      return true;
    }
  }

}



