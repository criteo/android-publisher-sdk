package com.criteo.pubsdk.Util;

import android.content.Context;

import com.criteo.pubsdk.R;


public class HostAppUtil {
    public static String getPublisherId(Context context){
        return context.getString(R.string.criteo_publisher_id);
}
}
