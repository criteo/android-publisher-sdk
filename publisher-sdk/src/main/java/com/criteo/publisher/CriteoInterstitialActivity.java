package com.criteo.publisher;

import static com.criteo.publisher.interstitial.InterstitialActivityHelper.CALLING_ACTIVITY;
import static com.criteo.publisher.interstitial.InterstitialActivityHelper.RESULT_RECEIVER;
import static com.criteo.publisher.interstitial.InterstitialActivityHelper.WEB_VIEW_DATA;
import static com.criteo.publisher.util.CriteoResultReceiver.ACTION_CLOSED;
import static com.criteo.publisher.util.CriteoResultReceiver.ACTION_LEFT_CLICKED;
import static com.criteo.publisher.util.CriteoResultReceiver.INTERSTITIAL_ACTION;
import static com.criteo.publisher.util.CriteoResultReceiver.RESULT_CODE_SUCCESSFUL;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.criteo.publisher.adview.AdWebViewClient;
import com.criteo.publisher.adview.AdWebViewListener;

public class CriteoInterstitialActivity extends Activity {

  private WebView webView;
  private ResultReceiver resultReceiver;
  private ImageButton closeButton;
  private RelativeLayout adLayout;
  private ComponentName callingActivityName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_criteo_interstitial);
    adLayout = findViewById(R.id.AdLayout);
    webView = findViewById(R.id.webview);
    closeButton = findViewById(R.id.closeButton);

    Bundle bundle = getIntent().getExtras();
    if (bundle != null && bundle.getString(WEB_VIEW_DATA) != null) {
      String webViewData = bundle.getString(WEB_VIEW_DATA);
      resultReceiver = bundle.getParcelable(RESULT_RECEIVER);
      callingActivityName = bundle.getParcelable(CALLING_ACTIVITY);

      prepareWebView();
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

  private void click() {
    Bundle bundle = new Bundle();
    bundle.putInt(INTERSTITIAL_ACTION, ACTION_LEFT_CLICKED);
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

    webView.setWebViewClient(new AdWebViewClient(new AdWebViewListener() {
      @Override
      public void onUserRedirectedToAd() {
        click();
      }

      @Override
      public void onUserBackFromAd() {
        close();
      }
    }, callingActivityName));
  }

  @Override
  public void onBackPressed() {
    close();
  }

  @VisibleForTesting
  WebView getWebView() {
    return webView;
  }

}



