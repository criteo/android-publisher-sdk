/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher;

import static com.criteo.publisher.ErrorLogMessage.onUncaughtErrorAtPublicApi;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.adview.AdWebViewClient;
import com.criteo.publisher.adview.RedirectionListener;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import java.lang.ref.WeakReference;

public class CriteoInterstitialActivity extends Activity {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private WebView webView;
  private ResultReceiver resultReceiver;
  private FrameLayout adLayout;
  private ComponentName callingActivityName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    try {
      super.onCreate(savedInstanceState);
      doOnCreate();
    } catch (Throwable t) {
      logger.log(onUncaughtErrorAtPublicApi(t));
      finish();
    }
  }

  private void doOnCreate() {
    setContentView(R.layout.activity_criteo_interstitial);
    adLayout = findViewById(R.id.AdLayout);

    /*
      {@link WebView}s leak the Activity context:
      {@link https://issuetracker.google.com/issues/36918787}. This happens when the {@link WebView}
      is created via the XML file. In order to avoid leaking the Activity context, a workaround
      consists in creating the WebView by hand by passing the Application context instead.
     */
    webView = new WebView(getApplicationContext());
    adLayout.addView(webView, 0);

    ImageButton closeButton = findViewById(R.id.closeButton);

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
    webView.setWebViewClient(null);
    webView.destroy();
    webView = null;
  }

  private void displayWebView(String webViewData) {
    webView.loadDataWithBaseURL("https://criteo.com", webViewData, "text/html", "UTF-8",
        "about:blank");
  }

  private void prepareWebView() {
    webView.getSettings().setJavaScriptEnabled(true);

    WeakRedirectionListener weakRedirectionListener = new WeakRedirectionListener(
        new WeakReference<>(this)
    );

    AdWebViewClient adWebViewClient = new AdWebViewClient(
        weakRedirectionListener,
        callingActivityName
    );

    webView.setWebViewClient(adWebViewClient);
  }

  @Override
  public void onBackPressed() {
    close();
  }

  @VisibleForTesting
  WebView getWebView() {
    return webView;
  }

  private static class WeakRedirectionListener implements RedirectionListener {

    private final WeakReference<CriteoInterstitialActivity> activityRef;

    private WeakRedirectionListener(WeakReference<CriteoInterstitialActivity> activityRef) {
      this.activityRef = activityRef;
    }

    @Override
    public void onUserRedirectedToAd() {
      CriteoInterstitialActivity criteoInterstitialActivity = activityRef.get();
      if (criteoInterstitialActivity != null) {
        criteoInterstitialActivity.click();
      }
    }

    @Override
    public void onUserBackFromAd() {
      CriteoInterstitialActivity criteoInterstitialActivity = activityRef.get();
      if (criteoInterstitialActivity != null) {
        criteoInterstitialActivity.close();
      }
    }
  }
}


