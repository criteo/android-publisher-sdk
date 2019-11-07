package com.criteo.publisher.model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import com.criteo.publisher.Util.UserAgentCallback;
import com.criteo.publisher.Util.UserAgentHandler;

public class DeviceInfo {
    private static final String TAG = DeviceInfo.class.getSimpleName();

    private static final String DEFAULT_USER_AGENT;

    private String resolvedUserAgent;

    static {
        DEFAULT_USER_AGENT = getDefaultUserAgent();
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
                // Capture the user-agent for internal use inside DeviceInfo
                resolvedUserAgent = resolveUserAgent(context);

                // Send the user-agent string forward to the userAgentCallback
                Message msg = mainHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("userAgent", resolvedUserAgent);
                msg.setData(bundle);
                mainHandler.sendMessage(msg);
            }

        };
        mainHandler.post(setUserAgentTask);

    }

    private static String getDefaultUserAgent()
    {
        String userAgent = null;

        try {
            userAgent = System.getProperty("http.agent");
        } catch (Throwable tr) {
            Log.e(TAG, "Unable to retrieve system user-agent.", tr);
        }

        return userAgent != null ? userAgent : "";
    }

    private static String resolveUserAgent(Context context) {
        String userAgent = null;

        // Try to fetch the UA from a web view
        // This may fail with a RuntimeException that is safe to ignore
        try {
            userAgent = getWebViewUserAgent(context);
        } catch (Throwable ignore) {
        }

        // If we failed to get a WebView UA, try to fall back to a system UA, instead
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = DEFAULT_USER_AGENT;
        }

        return userAgent;
    }

    private static String getWebViewUserAgent(Context context) {
        WebView webView = new WebView(context);
        String userAgent = webView.getSettings().getUserAgentString();
        webView.destroy();
        return userAgent;
    }

    public String getUserAgent() {
        return resolvedUserAgent;
    }
}