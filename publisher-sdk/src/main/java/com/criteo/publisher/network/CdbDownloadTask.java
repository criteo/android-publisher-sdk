package com.criteo.publisher.network;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.Util.LoggingUtil;
import com.criteo.publisher.Util.NetworkResponseListener;
import com.criteo.publisher.Util.UserPrivacyUtil;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.User;
import java.util.Hashtable;
import java.util.List;
import org.json.JSONObject;

public class CdbDownloadTask extends AsyncTask<Object, Void, NetworkResult> {

  private static final String TAG = "Criteo.CDT";

  /**
   * Profile ID used by the SDK, so CDB and the Supply chain can recognize that the request come
   * from the PublisherSDK.
   */
  private static final int PROFILE_ID = 235;

  private final boolean isConfigRequested;

  private boolean isCdbRequested;

  @NonNull
  private final DeviceInfo deviceInfo;

  @NonNull
  private final NetworkResponseListener responseListener;

  @NonNull
  private final List<CacheAdUnit> cacheAdUnits;

  @NonNull
  private final PubSdkApi api;

  @NonNull
  private final DeviceUtil deviceUtil;

  @NonNull
  private final LoggingUtil loggingUtil;

  @NonNull
  private final Hashtable<CacheAdUnit, CdbDownloadTask> bidsInCdbTask;

  @NonNull
  private final UserPrivacyUtil userPrivacyUtil;

  @NonNull
  private final User user;

  @NonNull
  private final Publisher publisher;

  public CdbDownloadTask(
      @NonNull NetworkResponseListener responseListener,
      boolean isConfigRequested,
      boolean isCdbRequested,
      @NonNull DeviceInfo deviceInfo,
      @NonNull List<CacheAdUnit> adUnits,
      @NonNull Hashtable<CacheAdUnit, CdbDownloadTask> bidsInMap,
      @NonNull DeviceUtil deviceUtil,
      @NonNull LoggingUtil loggingUtil,
      @NonNull UserPrivacyUtil userPrivacyUtil,
      @NonNull PubSdkApi api,
      @NonNull User user,
      @NonNull Publisher publisher) {
    this.responseListener = responseListener;
    this.isConfigRequested = isConfigRequested;
    this.isCdbRequested = isCdbRequested;
    this.deviceInfo = deviceInfo;
    this.cacheAdUnits = adUnits;
    this.bidsInCdbTask = bidsInMap;
    this.deviceUtil = deviceUtil;
    this.loggingUtil = loggingUtil;
    this.api = api;
    this.userPrivacyUtil = userPrivacyUtil;
    this.user = user;
    this.publisher = publisher;
  }

  @Nullable
  @Override
  protected NetworkResult doInBackground(Object... objects) {
    NetworkResult result = null;

    try {
      result = doCdbDownloadTask();
    } catch (Throwable tr) {
      Log.e(TAG, "Internal CDT exec error.", tr);
    }

    return result;
  }

  @NonNull
  private NetworkResult doCdbDownloadTask() throws Exception {
    JSONObject configResult = requestConfig();
    CdbResponse cdbResponse = requestCdb();
    return new NetworkResult(cdbResponse, configResult);
  }

  @Nullable
  private JSONObject requestConfig() {
    if (!isConfigRequested) {
      return null;
    }

    JSONObject configResult = api.loadConfig(
        publisher.getCriteoPublisherId(),
        publisher.getBundleId(),
        user.getSdkVersion());

    // FIXME EE-792 Proper solution would be to separate the remote config and the CDB calls
    //  This will remove the is*Requested boolean, this ugly partial parsing and partial
    //  result. NetworkResult would then have no meaning. Also, extracting the remote config
    //  call would let us organize properly the calls at startup (config, CDB, GUM).
    Boolean isKillSwitchEnabled = Config.parseKillSwitch(configResult);
    if (isKillSwitchEnabled != null && isKillSwitchEnabled) {
      isCdbRequested = false;
    }

    return configResult;
  }

  @Nullable
  private CdbResponse requestCdb() throws Exception {
    if (!isCdbRequested) {
      return null;
    }

    CdbRequest cdbRequest = buildCdbRequest();
    String userAgent = deviceInfo.getUserAgent().get();
    CdbResponse cdbResult = api.loadCdb(cdbRequest, userAgent);
    logCdbResponse(cdbResult);

    return cdbResult;
  }

  @NonNull
  private CdbRequest buildCdbRequest() {
    String advertisingId = deviceUtil.getAdvertisingId();
    if (!TextUtils.isEmpty(advertisingId)) {
      user.setDeviceId(advertisingId);
    }

    String uspIab = userPrivacyUtil.getIabUsPrivacyString();
    if (uspIab != null && !uspIab.isEmpty()) {
      user.setUspIab(uspIab);
    }

    String uspOptout = userPrivacyUtil.getUsPrivacyOptout();
    if (!uspOptout.isEmpty()) {
      user.setUspOptout(uspOptout);
    }

    String mopubConsent = userPrivacyUtil.getMopubConsent();
    if (!mopubConsent.isEmpty()) {
      user.setMopubConsent(mopubConsent);
    }

    return new CdbRequest(publisher, user, user.getSdkVersion(), PROFILE_ID,
        userPrivacyUtil.gdpr(), cacheAdUnits);
  }

  private void logCdbResponse(CdbResponse response) {
    if (loggingUtil.isLoggingEnabled() && response != null && response.getSlots().size() > 0) {
      StringBuilder builder = new StringBuilder();
      for (Slot slot : response.getSlots()) {
        builder.append(slot.toString());
        builder.append("\n");
      }
      Log.d(TAG, builder.toString());
    }
  }

  @Override
  protected void onPostExecute(@Nullable NetworkResult networkResult) {
    try {
      doOnPostExecute(networkResult);
    } catch (Throwable tr) {
      Log.e(TAG, "Internal CDT PostExec error.", tr);
    }
  }

  private void doOnPostExecute(@Nullable NetworkResult networkResult) {
    for (CacheAdUnit cacheAdUnit : cacheAdUnits) {
      bidsInCdbTask.remove(cacheAdUnit);
    }

    if (networkResult != null) {
      handleConfigResponse(networkResult.getConfig());
      handleCdbResponse(networkResult.getCdbResponse());
    }
  }

  private void handleConfigResponse(@Nullable JSONObject config) {
    if (config != null) {
      responseListener.refreshConfig(config);
    }
  }

  private void handleCdbResponse(@Nullable CdbResponse cdbResponse) {
    if (cdbResponse != null) {
      responseListener.setCacheAdUnits(cdbResponse.getSlots());
      responseListener.setTimeToNextCall(cdbResponse.getTimeToNextCall());
    }
  }
}
