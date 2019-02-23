package com.criteo.pubsdk.network;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.criteo.pubsdk.BuildConfig;
import com.criteo.pubsdk.Util.DeviceUtil;
import com.criteo.pubsdk.Util.HostAppUtil;
import com.criteo.pubsdk.Util.NetworkResponseListener;
import com.criteo.pubsdk.model.AdUnit;
import com.criteo.pubsdk.model.Cdb;
import com.criteo.pubsdk.model.Config;
import com.criteo.pubsdk.model.Publisher;
import com.criteo.pubsdk.model.Slot;
import com.criteo.pubsdk.model.User;

import org.json.JSONObject;

import java.util.ArrayList;

public class CdbDownloadTask extends AsyncTask<Object, Void, NetworkResult> {
    private static final String TAG = CdbDownloadTask.class.getSimpleName();
    private final Context mContext;
    private boolean callConfig;
    private String userAgent;
    private final NetworkResponseListener responseListener;

    public CdbDownloadTask(Context context, NetworkResponseListener responseListener, boolean callConfig, String userAgent) {
        this.mContext = context;
        this.responseListener = responseListener;
        this.callConfig = callConfig;
        this.userAgent = userAgent;
    }

    @Override
    protected NetworkResult doInBackground(Object... objects) {
        if (objects.length < 4) {
            return null;
        }
        int profile = (Integer) objects[0];
        User user = (User) objects[1];
        Publisher publisher = (Publisher) objects[2];
        ArrayList<AdUnit> adUnits = (ArrayList<AdUnit>) objects[3];
        if (profile <= 0 || adUnits.size() == 0) {
            return null;
        }
        if (DeviceUtil.hasPlayServices(mContext)) {
            String advertisingId = DeviceUtil.getAdvertisingId(mContext);
            if (!TextUtils.isEmpty(advertisingId)) {
                user.setDeviceId(advertisingId);
            }
        }
        NetworkResult result = new NetworkResult();
        Config configResult = null;
        if (callConfig) {
            configResult = PubSdkNetwork.loadConfig(mContext, publisher.getNetworkId(),
                    publisher.getBundleId(), user.getSdkVer());
            if (configResult != null && configResult.isKillSwitch()) {
                result.setConfig(configResult);
                return result;
            }
        }
        Cdb cdbRequest = new Cdb();
        cdbRequest.setAdUnits(adUnits);
        cdbRequest.setUser(user);
        cdbRequest.setPublisher(publisher);
        cdbRequest.setSdkVersion(String.valueOf(BuildConfig.VERSION_NAME));
        cdbRequest.setProfileId(profile);
        JSONObject gdpr = HostAppUtil.gdpr(mContext.getApplicationContext());
        if (gdpr != null) {
            cdbRequest.setGdprConsent(gdpr);
        }
        Cdb cdbResult = PubSdkNetwork.loadCdb(mContext, cdbRequest, userAgent);
        if (cdbResult != null && cdbResult.getSlots() != null
                && cdbResult.getSlots().size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (Slot slot : cdbResult.getSlots()) {
                builder.append(slot.toString());
                builder.append("\n");
            }
            Log.d(TAG, builder.toString());
        }
        return new NetworkResult(cdbResult, configResult);
    }

    @Override
    protected void onPostExecute(NetworkResult networkResult) {
        super.onPostExecute(networkResult);
        if (responseListener != null && networkResult != null) {
            if (networkResult.getCdb() != null) {
                responseListener.setAdUnits(networkResult.getCdb().getSlots());
                responseListener.setTimeToNextCall(networkResult.getCdb().getTimeToNextCall());
            }
            if (networkResult.getConfig() != null) {
                responseListener.setConfig(networkResult.getConfig());
            }

        }
    }
}
