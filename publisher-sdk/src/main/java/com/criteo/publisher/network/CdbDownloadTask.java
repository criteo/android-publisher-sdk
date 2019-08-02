package com.criteo.publisher.network;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.criteo.publisher.BuildConfig;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.HostAppUtil;
import com.criteo.publisher.Util.NetworkResponseListener;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Cdb;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.User;
import java.util.ArrayList;
import org.json.JSONObject;

public class CdbDownloadTask extends AsyncTask<Object, Void, NetworkResult> {

    private static final String TAG = "Criteo.CDT";
    private final Context mContext;
    private final boolean callConfig;
    private final String userAgent;
    private final NetworkResponseListener responseListener;

    public CdbDownloadTask(Context context, NetworkResponseListener responseListener, boolean callConfig,
            String userAgent) {
        this.mContext = context.getApplicationContext();
        this.responseListener = responseListener;
        this.callConfig = callConfig;
        this.userAgent = userAgent;
    }

    @Override
    protected NetworkResult doInBackground(Object... objects) {
        NetworkResult result = null;

        try {
            result = doCdbDownloadTask(objects);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal CDT exec error.", tr);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private NetworkResult doCdbDownloadTask(Object[] objects) {
        if (objects.length < 4) {
            return null;
        }
        int profile = (Integer) objects[0];
        User user = (User) objects[1];
        Publisher publisher = (Publisher) objects[2];
        ArrayList<CacheAdUnit> cacheAdUnits = (ArrayList<CacheAdUnit>) objects[3];
        if (profile <= 0) {
            return null;
        }
        if (DeviceUtil.hasPlayServices(mContext)) {
            String advertisingId = DeviceUtil.getAdvertisingId(mContext);

            if (!TextUtils.isEmpty(advertisingId)) {
                user.setDeviceId(advertisingId);
            }
        }
        NetworkResult result = new NetworkResult();
        JSONObject configResult = null;
        if (callConfig) {
            configResult = PubSdkNetwork.loadConfig(mContext, publisher.getCriteoPublisherId(),
                    publisher.getBundleId(), user.getSdkVer());
            if (configResult != null) {
                result.setConfig(configResult);
            }
        }
        Cdb cdbRequest = new Cdb();
        cdbRequest.setCacheAdUnits(cacheAdUnits);
        cdbRequest.setUser(user);
        cdbRequest.setPublisher(publisher);
        cdbRequest.setSdkVersion(String.valueOf(BuildConfig.VERSION_NAME));
        cdbRequest.setProfileId(profile);
        JSONObject gdpr = HostAppUtil.gdpr(mContext.getApplicationContext());
        if (gdpr != null) {
            cdbRequest.setGdprConsent(gdpr);
        }
        Cdb cdbResult = PubSdkNetwork.loadCdb(mContext, cdbRequest, userAgent);
        if (DeviceUtil.isLoggingEnabled() && cdbResult != null && cdbResult.getSlots() != null
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
        try {
            doOnPostExecute(networkResult);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal CDT PostExec error.", tr);
        }
    }

    private void doOnPostExecute(NetworkResult networkResult) {
        super.onPostExecute(networkResult);
        if (responseListener != null && networkResult != null) {
            if (networkResult.getCdb() != null) {
                responseListener.setCacheAdUnits(networkResult.getCdb().getSlots());
                responseListener.setTimeToNextCall(networkResult.getCdb().getTimeToNextCall());
            }
            if (networkResult.getConfig() != null) {
                responseListener.refreshConfig(networkResult.getConfig());
            }
        }
    }
}
