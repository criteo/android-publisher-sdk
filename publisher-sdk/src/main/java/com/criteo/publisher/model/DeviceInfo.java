package com.criteo.publisher.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import com.criteo.publisher.Util.CompletableFuture;
import com.criteo.publisher.Util.UserAgentCallback;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceInfo {
    private static final String TAG = DeviceInfo.class.getSimpleName();

    private final Context context;
    private final CompletableFuture<String> userAgentFuture = new CompletableFuture<>();
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public DeviceInfo(Context context) {
        this.context = context;
    }

    public void initialize(@NonNull UserAgentCallback userAgentCallback) {
        // This needs to be run on UI thread because a WebView is used to fetch the user-agent
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isInitialized.compareAndSet(false, true)) {
                    String userAgent = resolveUserAgent();
                    userAgentFuture.complete(userAgent);
                }

                // Notify the userAgentCallback that the user agent is ready
                userAgentCallback.done();
            }
        });
    }

    @NonNull
    public Future<String> getUserAgent() {
        // Initialize automatically so that it's safe to call this method alone.
        initialize(new UserAgentCallback() {
            @Override
            public void done() {
            }
        });

        return userAgentFuture;
    }

    private void runOnUiThread(Runnable runnable) {
        Runnable safeRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable tr) {
                    Log.e(TAG, "Internal error while setting user-agent.", tr);
                }
            }
        };

        if (Thread.currentThread() != context.getMainLooper().getThread()) {
            mainHandler.post(safeRunnable);
        } else {
            safeRunnable.run();
        }
    }

    @VisibleForTesting
    @NonNull
    @UiThread
    String resolveUserAgent() {
        String userAgent = null;

        // Try to fetch the UA from a web view
        // This may fail with a RuntimeException that is safe to ignore
        try {
            userAgent = getWebViewUserAgent();
        } catch (Throwable ignore) {
            // FIXME this is not a RuntimeException, this is a throwable that should not be
            //  catch and ignore so easily.
        }

        // If we failed to get a WebView UA, try to fall back to a system UA, instead
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = getDefaultUserAgent();
        }

        return userAgent;
    }

    @UiThread
    private String getWebViewUserAgent() {
        WebView webView = new WebView(context);
        String userAgent = webView.getSettings().getUserAgentString();
        webView.destroy();
        return userAgent;
    }

    @NonNull
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
}