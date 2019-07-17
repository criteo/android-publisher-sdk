package com.criteo.publisher.view;

import static com.criteo.publisher.Util.CriteoResultReceiver.ACTION_CLICKED;
import static com.criteo.publisher.Util.CriteoResultReceiver.ACTION_CLOSED;
import static com.criteo.publisher.Util.CriteoResultReceiver.INTERSTITIAL_ACTION;
import static com.criteo.publisher.Util.CriteoResultReceiver.RESULT_CODE_SUCCESSFUL;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.criteo.publisher.R;

public class CriteoInterstitialActivity extends Activity {

    private static final int DISMISS_TIME = 7;

    private WebView webView;
    private ResultReceiver resultReceiver;
    private ImageButton closeButton;
    private Handler handler;
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
        if (bundle != null && bundle.getString("webviewdata") != null) {
            String webViewData = bundle.getString("webviewdata");
            resultReceiver = bundle.getParcelable("resultreceiver");
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
        waitAndDismiss();
        webView.loadDataWithBaseURL("about:blank", webViewData, "text/html", "UTF-8", "about:blank");
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    private void waitAndDismiss() {
        handler = new Handler();
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                close();
            }
        };
        handler.postDelayed(runnableCode, DISMISS_TIME * 1000);
    }

    private void prepareWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new InterstitialWebViewClient());
    }

    @Override
    public void onBackPressed() {
        close();
    }

    private class InterstitialWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.getContext().startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            Bundle bundle = new Bundle();
            bundle.putInt(INTERSTITIAL_ACTION, ACTION_CLICKED);
            resultReceiver.send(RESULT_CODE_SUCCESSFUL, bundle);
            finish();
            return true;
        }
    }

}



