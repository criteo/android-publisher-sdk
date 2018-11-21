package com.criteo.pubsdk.network;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.criteo.pubsdk.Util.DeviceUtil;
import com.criteo.pubsdk.model.Publisher;
import com.criteo.pubsdk.model.Slot;
import com.criteo.pubsdk.model.User;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class CdbDownloadTask extends AsyncTask<Object, Void, Void> {
    private final Context mContext;
    private static final String TAG = CdbDownloadTask.class.getSimpleName();

    public CdbDownloadTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Object... objects) {
        String profile = (String) objects[0];
        User user = (User) objects[1];
        Publisher publisher = (Publisher) objects[2];
        ArrayList<Slot> slots = (ArrayList<Slot>) objects[3];
        if (DeviceUtil.hasPlayServices(mContext)) {
            String addId = DeviceUtil.getAdvertisingId(mContext);
            if (!TextUtils.isEmpty(addId)) {
                user.setDeviceId(addId);
            }
        }
        JsonObject object = RestAPI.callCdb(profile, user, publisher, slots);
        Log.d(TAG, object.toString());
        return null;
    }
}
