package com.criteo.pubsdk.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.criteo.pubsdk.Util.AppEventResponseListener;
import com.criteo.pubsdk.Util.DeviceUtil;
import com.google.gson.JsonObject;

public class AppEventTask extends AsyncTask<Object, Void, JsonObject> {
    private static final String TAG = AppEventTask.class.getSimpleName();
    private static final int SENDER_ID = 1000;
    private static final String THROTTLE = "throttleSec";
    private final Context mContext;
    private AppEventResponseListener responseListener;

    public AppEventTask(Context context, AppEventResponseListener eventResponseListener) {
        this.mContext = context;
        this.responseListener = eventResponseListener;
    }

    @Override
    protected JsonObject doInBackground(Object... objects) {
        String eventType = (String) objects[0];
        int limitedAdTracking = 0;
        String gaid = null;
        if (DeviceUtil.hasPlayServices(mContext)) {
            limitedAdTracking = DeviceUtil.isLimitAdTrackingEnabled(mContext);
            gaid = DeviceUtil.getAdvertisingId(mContext);

        }
        String appId = mContext.getApplicationContext().getPackageName();
        JsonObject response = PubSdkNetwork.postEvent(mContext, SENDER_ID, appId, gaid, eventType, limitedAdTracking);
        if (response != null) {
            Log.d(TAG, response.toString());
        }
        return response;
    }

    @Override
    protected void onPostExecute(JsonObject result) {
        super.onPostExecute(result);
        if (responseListener != null && result != null && result.has(THROTTLE)) {
            responseListener.setThreshold(result.get(THROTTLE).getAsInt());
        } else {
            responseListener.setThreshold(0);
        }
    }
}
