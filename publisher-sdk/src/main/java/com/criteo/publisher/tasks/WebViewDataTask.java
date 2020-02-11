package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
import com.criteo.publisher.Util.StreamUtil;
import com.criteo.publisher.Util.TextUtils;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.WebViewData;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebViewDataTask extends AsyncTask<String, Void, String> {

  private static final String TAG = "Criteo.WVDT";

  @NonNull
  private WebViewData webviewData;

  @NonNull
  private final DeviceInfo deviceInfo;

  @Nullable
  private CriteoInterstitialAdDisplayListener criteoInterstitialAdDisplayListener;

  public WebViewDataTask(@NonNull WebViewData webviewData,
      @NonNull DeviceInfo deviceInfo,
      @Nullable CriteoInterstitialAdDisplayListener adDisplayListener) {
    this.webviewData = webviewData;
    this.deviceInfo = deviceInfo;
    this.criteoInterstitialAdDisplayListener = adDisplayListener;
  }

  @Override
  protected String doInBackground(String... args) {
    try {
      return doWebViewDataTask(args);
    } catch (Throwable tr) {
      Log.e(TAG, "Internal WVDT exec error.", tr);
    }

    return null;
  }

  private String doWebViewDataTask(String[] args) throws Exception {
    String displayUrl = args[0];
    String userAgent = deviceInfo.getUserAgent().get();

    URL url = new URL(displayUrl);

    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    urlConnection.setRequestMethod("GET");
    urlConnection.setRequestProperty("Content-Type", "text/plain");
    if (!TextUtils.isEmpty(userAgent)) {
      urlConnection.setRequestProperty("User-Agent", userAgent);
    }

    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
      return StreamUtil.readStream(urlConnection.getInputStream());
    }

    return null;
  }

  @Override
  protected void onPostExecute(@Nullable String data) {
    try {
      doOnPostExecute(data);
    } catch (Throwable tr) {
      Log.e(TAG, "Internal WVDT PostExec error.", tr);
    }
  }

  private void doOnPostExecute(@Nullable String data) {
    if (TextUtils.isEmpty(data)) {
      webviewData.downloadFailed();
      if (criteoInterstitialAdDisplayListener != null) {
        criteoInterstitialAdDisplayListener
            .onAdFailedToDisplay(CriteoErrorCode.ERROR_CODE_NETWORK_ERROR);
      }
    } else {
      webviewData.setContent(data);
      webviewData.downloadSucceeded();
      if (criteoInterstitialAdDisplayListener != null) {
        criteoInterstitialAdDisplayListener.onAdReadyToDisplay();
      }
    }
  }

}
