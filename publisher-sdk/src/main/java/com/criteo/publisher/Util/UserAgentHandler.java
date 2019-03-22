package com.criteo.publisher.Util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.criteo.publisher.Util.UserAgentCallback;


/**
 * Handler class that gets useragent message from main thread and fires callback
 */
public class UserAgentHandler extends Handler {

    UserAgentCallback userAgentCallback;

    public UserAgentHandler(Looper looper, UserAgentCallback callback) {
        super(looper);
        this.userAgentCallback = callback;

    }

    @SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
    @Override
    public void handleMessage(Message msg) {
        Bundle bundle = msg.getData();
        String userAgent = bundle.getString("userAgent");
        if (userAgent != null) {
            userAgentCallback.done(userAgent);
        }
    }
}