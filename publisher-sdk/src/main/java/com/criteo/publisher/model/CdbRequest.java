package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CdbRequest {

  private static final String PUBLISHER = "publisher";
  private static final String USER = "user";
  private static final String SDK_VERSION = "sdkVersion";
  private static final String PROFILE_ID = "profileId";
  private static final String GDPR_CONSENT = "gdprConsent";
  private static final String SLOTS = "slots";

  @NonNull
  private final Publisher publisher;

  @NonNull
  private final User user;

  @NonNull
  private final String sdkVersion;

  private final int profileId;

  @Nullable
  private final JSONObject gdprConsent;

  @NonNull
  private final List<CacheAdUnit> adUnits;

  public CdbRequest(
      @NonNull Publisher publisher,
      @NonNull User user,
      @NonNull String sdkVersion,
      int profileId,
      @Nullable JSONObject gdprConsent,
      @NonNull List<CacheAdUnit> adUnits) {
    this.publisher = publisher;
    this.user = user;
    this.sdkVersion = sdkVersion;
    this.profileId = profileId;
    this.gdprConsent = gdprConsent;
    this.adUnits = adUnits;
  }

  @NonNull
  public Publisher getPublisher() {
    return publisher;
  }

  @NonNull
  public User getUser() {
    return user;
  }

  @NonNull
  public String getSdkVersion() {
    return sdkVersion;
  }

  public int getProfileId() {
    return profileId;
  }

  @Nullable
  public JSONObject getGdprConsent() {
    return gdprConsent;
  }

  @NonNull
  public List<CacheAdUnit> getAdUnits() {
    return adUnits;
  }

  @NonNull
  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(USER, user.toJson());
    json.put(PUBLISHER, publisher.toJson());
    json.put(SDK_VERSION, sdkVersion);
    json.put(PROFILE_ID, profileId);

    JSONArray jsonAdUnits = new JSONArray();
    for (CacheAdUnit adUnit : adUnits) {
      jsonAdUnits.put(adUnit.toJson());
    }
    if (jsonAdUnits.length() > 0) {
      json.put(SLOTS, jsonAdUnits);
    }
    if (gdprConsent != null) {
      json.put(GDPR_CONSENT, gdprConsent);
    }
    return json;
  }

}
