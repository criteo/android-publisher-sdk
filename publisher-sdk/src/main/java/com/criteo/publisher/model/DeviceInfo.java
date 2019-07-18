package com.criteo.publisher.model;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.UserAgentCallback;
import com.criteo.publisher.Util.UserAgentHandler;

public class DeviceInfo {

    private String webViewUserAgent;
    private final Context context;

    public DeviceInfo(final Context context) {
        this.context = context;
        initialize();
    }

    private void initialize() {

        final Handler mainHandler = new UserAgentHandler(Looper.getMainLooper(), new UserAgentCallback() {
            @Override
            public void done(String useragent) {
                webViewUserAgent = useragent;

            }
        });

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

                String taskUserAgent = DeviceUtil.getUserAgent(context);
                Message msg = mainHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("userAgent", taskUserAgent);
                msg.setData(bundle);
                mainHandler.sendMessage(msg);

            }

        };
        mainHandler.post(setUserAgentTask);

    }

    public String getWebViewUserAgent() {
        return webViewUserAgent;
    }
}


