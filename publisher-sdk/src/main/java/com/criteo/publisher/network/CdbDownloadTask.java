package com.criteo.publisher.network;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.criteo.publisher.BuildConfig;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.UserPrivacyUtil;
import com.criteo.publisher.Util.LoggingUtil;
import com.criteo.publisher.Util.NetworkResponseListener;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Cdb;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.User;
import java.util.Hashtable;
import java.util.List;
import org.json.JSONObject;

public class CdbDownloadTask extends AsyncTask<Object, Void, NetworkResult> {
    private static final String TAG = "Criteo.CDT";

    private final Context mContext;
    private final boolean callConfig;
    private final String userAgent;
    private final NetworkResponseListener responseListener;
    private final List<CacheAdUnit> cacheAdUnits;
    private final PubSdkApi api;
    private final DeviceUtil deviceUtil;
    private final LoggingUtil loggingUtil;
    private final Hashtable<CacheAdUnit, CdbDownloadTask> bidsInCdbTask;
    private final UserPrivacyUtil userPrivacyUtil;

    public CdbDownloadTask(
        Context context,
        NetworkResponseListener responseListener,
        boolean callConfig,
        String userAgent,
        List<CacheAdUnit> adUnits,
        Hashtable<CacheAdUnit, CdbDownloadTask> bidsInMap,
        DeviceUtil deviceUtil,
        LoggingUtil loggingUtil,
        UserPrivacyUtil userPrivacyUtil
    ) {
        this.mContext = context.getApplicationContext();
        this.responseListener = responseListener;
        this.callConfig = callConfig;
        this.userAgent = userAgent;
        this.cacheAdUnits = adUnits;
        this.bidsInCdbTask = bidsInMap;
        this.deviceUtil = deviceUtil;
        this.loggingUtil = loggingUtil;
        this.api = DependencyProvider.getInstance().providePubSdkApi();
        this.userPrivacyUtil = userPrivacyUtil;
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

    private NetworkResult doCdbDownloadTask(Object[] objects) {
        if (objects.length < 3) {
            return null;
        }
        int profile = (Integer) objects[0];
        User user = (User) objects[1];
        Publisher publisher = (Publisher) objects[2];
        if (profile <= 0) {
            return null;
        }

        String advertisingId = deviceUtil.getAdvertisingId();
        if (!TextUtils.isEmpty(advertisingId)) {
            user.setDeviceId(advertisingId);
        }

        NetworkResult result = new NetworkResult();
        JSONObject configResult = null;
        if (callConfig) {
            configResult = api.loadConfig(mContext, publisher.getCriteoPublisherId(),
                    publisher.getBundleId(), user.getSdkVer());
            if (configResult != null) {
                result.setConfig(configResult);
            }
        }

        String uspIab =  userPrivacyUtil.getIabUsPrivacyString();
        if (uspIab != null && !uspIab.isEmpty()) {
            user.setUspIab(uspIab);
        }

        Cdb cdbRequest = new Cdb();
        cdbRequest.setCacheAdUnits(cacheAdUnits);
        cdbRequest.setUser(user);
        cdbRequest.setPublisher(publisher);
        cdbRequest.setSdkVersion(BuildConfig.VERSION_NAME);
        cdbRequest.setProfileId(profile);
        JSONObject gdpr = userPrivacyUtil.gdpr();
        if (gdpr != null) {
            cdbRequest.setGdprConsent(gdpr);
        }
        Cdb cdbResult = api.loadCdb(mContext, cdbRequest, userAgent);
        if (loggingUtil.isLoggingEnabled() && cdbResult != null && cdbResult.getSlots() != null
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

        for (CacheAdUnit cacheAdUnit : cacheAdUnits) {
            bidsInCdbTask.remove(cacheAdUnit);
        }

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
