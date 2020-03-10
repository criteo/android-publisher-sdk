package com.criteo.publisher.network;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.criteo.publisher.Util.LoggingUtil;
import com.criteo.publisher.Util.NetworkResponseListener;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbRequestFactory;
import com.criteo.publisher.model.CdbResponse;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.RemoteConfigRequest;
import com.criteo.publisher.model.RemoteConfigRequestFactory;
import com.criteo.publisher.model.Slot;
import java.util.Hashtable;
import java.util.List;
import org.json.JSONObject;

public class CdbDownloadTask extends AsyncTask<Object, Void, NetworkResult> {

  private static final String TAG = "Criteo.CDT";

  private final boolean isConfigRequested;

  private boolean isCdbRequested;

  @NonNull
  private final NetworkResponseListener responseListener;

  @NonNull
  private final List<CacheAdUnit> cacheAdUnits;

  @NonNull
  private final PubSdkApi api;

  @NonNull
  private final LoggingUtil loggingUtil;

  @NonNull
  private final Hashtable<CacheAdUnit, CdbDownloadTask> bidsInCdbTask;

  @NonNull
  private final CdbRequestFactory cdbRequestFactory;

  @NonNull
  private final RemoteConfigRequestFactory remoteConfigRequestFactory;

  public CdbDownloadTask(
      @NonNull NetworkResponseListener responseListener,
      boolean isConfigRequested,
      boolean isCdbRequested,
      @NonNull List<CacheAdUnit> adUnits,
      @NonNull Hashtable<CacheAdUnit, CdbDownloadTask> bidsInMap,
      @NonNull LoggingUtil loggingUtil,
      @NonNull PubSdkApi api,
      @NonNull CdbRequestFactory cdbRequestFactory,
      @NonNull RemoteConfigRequestFactory remoteConfigRequestFactory) {
    this.responseListener = responseListener;
    this.isConfigRequested = isConfigRequested;
    this.isCdbRequested = isCdbRequested;
    this.cacheAdUnits = adUnits;
    this.bidsInCdbTask = bidsInMap;
    this.loggingUtil = loggingUtil;
    this.api = api;
    this.cdbRequestFactory = cdbRequestFactory;
    this.remoteConfigRequestFactory = remoteConfigRequestFactory;
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

    RemoteConfigRequest request = remoteConfigRequestFactory.createRequest();
    JSONObject configResult = api.loadConfig(request);

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

    CdbRequest cdbRequest = cdbRequestFactory.createRequest(cacheAdUnits);
    String userAgent = cdbRequestFactory.getUserAgent().get();
    CdbResponse cdbResult = api.loadCdb(cdbRequest, userAgent);
    logCdbResponse(cdbResult);

    return cdbResult;
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
