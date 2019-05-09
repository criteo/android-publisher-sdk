package com.criteo.publisher.mediation.view;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.R;

public class CriteoInterstitialActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criteo_interstitial);
        webView = findViewById(R.id.webview);

        prepareWebView();

        Bundle bundle = getIntent().getExtras();
        String webViewData = bundle.getString("webviewdata");

        if (webViewData != null) {
            displayWebView(webViewData);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
        webView = null;
    }

    private void displayWebView(String webViewData) {
        webView.loadDataWithBaseURL("about:blank", webViewData, "text/html", "UTF-8", "about:blank");
    }

    private void prepareWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new InterstitialWebViewClient());
    }

    private class InterstitialWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

}



