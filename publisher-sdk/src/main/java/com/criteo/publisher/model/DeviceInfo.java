package com.criteo.publisher.model;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;
import com.criteo.publisher.Util.UserAgentCallback;
import com.criteo.publisher.Util.UserAgentHandler;

public class DeviceInfo {

    private String webViewUserAgent;

    public DeviceInfo() {
    }

    public void initialize(final Context context, UserAgentCallback userAgentCallback) {

        final Handler mainHandler = new UserAgentHandler(Looper.getMainLooper(), userAgentCallback);

        final Runnable setUserAgentTask = new Runnable() {
            @Override
            public void run() {
                try {
                    doSetUserAgentTask();
                } catch (Throwable tr) {
                    Log.e(TAG, "Internal error while setting user-agent.", tr);
                }
            }

            private void doSetUserAgentTask() {

                String userAgent = getUserAgent(context);

                // Capture the user-agent for internal use inside DeviceInfo
                webViewUserAgent = userAgent;

                // Send the user-agent string forward to the userAgentCallback
                Message msg = mainHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("userAgent", userAgent);
                msg.setData(bundle);
                mainHandler.sendMessage(msg);
            }

        };
        mainHandler.post(setUserAgentTask);

    }

    private String getUserAgent(Context context) {
        WebView webView = new WebView(context);
        String userAgent = webView.getSettings().getUserAgentString();
        webView.destroy();
        return userAgent;
    }

    public String getWebViewUserAgent() {
        return webViewUserAgent;
    }
}