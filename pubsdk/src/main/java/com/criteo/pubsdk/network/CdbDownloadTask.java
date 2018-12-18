package com.criteo.pubsdk.network;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.criteo.pubsdk.BuildConfig;
import com.criteo.pubsdk.R;
import com.criteo.pubsdk.Util.DeviceUtil;
import com.criteo.pubsdk.model.Cdb;
import com.criteo.pubsdk.model.Publisher;
import com.criteo.pubsdk.model.Slot;
import com.criteo.pubsdk.model.User;

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
        Cdb cdb = new Cdb();
        cdb.setSlots(slots);
        cdb.setUser(user);
        cdb.setPublisher(publisher);
        cdb.setSdkVersion(mContext.getString(BuildConfig.VERSION_CODE));
        cdb.setProfileId(profile);
        Cdb response = PubSdkNetwork.loadCdb(cdb);
        if (response != null && response.getSlots() != null && response.getSlots().size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (Slot slot : response.getSlots()) {
                builder.append(slot.toString());
                builder.append("\n");
            }
            Log.d(TAG, builder.toString());
        }
        return null;
    }
}
