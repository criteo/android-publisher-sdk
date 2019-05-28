package com.criteo.publisher.mediation.view;

import static com.criteo.publisher.Util.CriteoResultReceiver.ACTION_CLICKED;
import static com.criteo.publisher.Util.CriteoResultReceiver.ACTION_CLOSED;
import static com.criteo.publisher.Util.CriteoResultReceiver.INTERSTITIAL_ACTION;
import static com.criteo.publisher.Util.CriteoResultReceiver.RESULT_CODE_SUCCESSFUL;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import com.criteo.publisher.R;

public class CriteoInterstitialActivity extends Activity {

    private WebView webView;
    private ResultReceiver resultReceiver;
    private ImageButton closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criteo_interstitial);
        webView = findViewById(R.id.webview);
        closeButton = findViewById(R.id.closeButton);

        prepareWebView();

        Bundle bundle = getIntent().getExtras();
        String webViewData = bundle.getString("webviewdata");
        resultReceiver = bundle.getParcelable("resultreceiver");

        if (webViewData != null) {
            displayWebView(webViewData);
        }

        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(INTERSTITIAL_ACTION, ACTION_CLOSED);
                resultReceiver.send(RESULT_CODE_SUCCESSFUL, bundle);
                finish();
            }
        });


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



