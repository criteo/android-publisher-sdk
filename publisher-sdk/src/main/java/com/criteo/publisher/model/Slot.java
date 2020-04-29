package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.criteo.publisher.Clock;
import com.criteo.publisher.model.nativeads.NativeAssets;
import com.criteo.publisher.util.URLUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class Slot {

  private static final String TAG = Slot.class.getSimpleName();
  private static final String CPM = "cpm";
  private static final String CURRENCY = "currency";
  private static final String HEIGHT = "height";
  private static final String WIDTH = "width";
  private static final String PLACEMENT_ID = "placementId";
  private static final String IMPRESSION_ID = "impId";
  private static final String NATIVE = "native";
  private static final String TTL = "ttl";
  private static final String DISPLAY_URL = "displayUrl";

  private static final int SECOND_TO_MILLI = 1000;

  @Nullable
  private final String impressionId;

  private String cpm;
  private String currency;
  private int width;
  private int height;
  private String placementId;
  private String displayUrl;
  private int ttl;
  //required for cache
  private long timeOfDownload;
  private double cpmValue;
  private NativeAssets nativeAssets;
  private boolean isNative;

  public Slot(JSONObject json) {
    impressionId = json.optString(IMPRESSION_ID, null);
    placementId = json.optString(PLACEMENT_ID, null);
    if (json.has(CPM)) {
      try {
        cpm = json.getString(CPM);
      } catch (JSONException e) {
        Log.d(TAG, "Unable to parse CPM " + e.getMessage());
        double cpmInt = json.optDouble(CPM, 0.0);
        cpm = String.valueOf(cpmInt);
      }
    } else {
      cpm = "0.0";
    }
    currency = json.optString(CURRENCY, null);
    width = json.optInt(WIDTH, 0);
    height = json.optInt(HEIGHT, 0);
    displayUrl = json.optString(DISPLAY_URL, null);
    ttl = json.optInt(TTL, 0);
    if (getCpmAsNumber() == null) {
      cpmValue = 0.0;
    }
    if (json.has(NATIVE)) {
      isNative = true;
      try {
        JSONObject jsonNative = json.getJSONObject(NATIVE);
        this.nativeAssets = new NativeAssets(jsonNative);
      } catch (Exception ex) {
        Log.d(TAG, "exception when parsing json" + ex.getLocalizedMessage());
        this.nativeAssets = null;
      }
    }
  }

  @Nullable
  public String getImpressionId() {
    return impressionId;
  }

  public boolean isNative() {
    return this.isNative;
  }

  public String getPlacementId() {
    return placementId;
  }

  public String getCpm() {
    return cpm;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public String getCurrency() {
    return currency;
  }

  /**
   * Returns the TTL in seconds for this bid response.
   */
  public int getTtl() {
    return ttl;
  }

  public void setTtl(int ttl) {
    this.ttl = ttl;
  }

  /**
   * Return the time of download in milliseconds for this bid response. This time represent a
   * client-side time given by a {@link com.criteo.publisher.Clock}.
   */
  public long getTimeOfDownload() {
    return timeOfDownload;
  }

  public void setTimeOfDownload(long timeOfDownload) {
    this.timeOfDownload = timeOfDownload;
  }

  /**
   * Returns the URL of the AJS creative to load for displaying the ad.
   * <p>
   * Non null after validation through {@link #isValid()}
   *
   * @return display URL
   */
  public String getDisplayUrl() {
    return displayUrl;
  }

  public NativeAssets getNativeAssets() {
    return this.nativeAssets;
  }

  public boolean isExpired(@NonNull Clock clock) {
    long expiryTimeMillis = ttl * SECOND_TO_MILLI + timeOfDownload;
    return expiryTimeMillis <= clock.getCurrentTimeInMillis();
  }

  @NonNull
  @Override
  public String toString() {
    return "Slot{" +
        " cpm=" + cpm +
        ", currency='" + currency + '\'' +
        ", width=" + width +
        ", height=" + height +
        ", placementId='" + placementId + '\'' +
        ", displayUrl='" + displayUrl + '\'' +
        ", ttl=" + ttl +
        ", timeOfDownload=" + timeOfDownload +
        '}';
  }

  /// Checks if displayUrl is not null/empty
  /// and if cpm is a valid number > 0
  public boolean isValid() {
    //check cpm
    Double testCpm = this.getCpmAsNumber();
    if (testCpm == null || testCpm < 0.0d) {
      return false;
    }

    // Check display Url
    if (!isNative && !URLUtil.isValidUrl(displayUrl)) {
      return false;
    }

    // if a slot is for Native but the required native assets are missing then mark the
    // bid invalid
    if (this.isNative) {
      if (this.nativeAssets == null) {
        return false;
      } else {
        return this.nativeAssets.getNativeProducts() != null
            && this.nativeAssets.getNativeProducts().size() != 0
            && !this.nativeAssets.getPrivacyOptOutImageUrl().equals("")
            && !this.nativeAssets.getPrivacyOptOutClickUrl().equals("")
            && this.nativeAssets.getImpressionPixels() != null
            && this.nativeAssets.getImpressionPixels().size() != 0;
      }
    }
    return true;
  }

  // Get the cpm as a double
  // Returns null if conversion fails
  // use isValid() to check if Slot is a valid slot
  public Double getCpmAsNumber() {
    try {
      this.cpmValue = Double.parseDouble(getCpm());
    } catch (Exception ex) {
      Log.d(TAG, "CPM is not a valid double " + ex.getMessage());
      return null;
    }
    return this.cpmValue;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Slot) {
      Slot other = (Slot) obj;
      return ((this.placementId == other.placementId || this.placementId.equals(other.placementId))
          &&
          (this.cpm == other.cpm || this.cpm.equals(other.cpm)) &&
          (this.currency == other.currency || this.currency.equals(other.currency)) &&
          this.width == other.width &&
          this.height == other.height &&
          this.ttl == other.ttl &&
          (this.displayUrl == other.displayUrl || this.displayUrl.equals(other.displayUrl)) &&
          (this.nativeAssets == other.nativeAssets || this.nativeAssets
              .equals(other.nativeAssets)));
    }
    return false;
  }

}
