package com.criteo.publisher.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.privacy.gdpr.GdprData;
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
  private final GdprData gdprData;

  @NonNull
  private final List<CdbRequestSlot> slots;

  public CdbRequest(
      @NonNull Publisher publisher,
      @NonNull User user,
      @NonNull String sdkVersion,
      int profileId,
      @Nullable GdprData gdprData,
      @NonNull List<CdbRequestSlot> slots) {
    this.publisher = publisher;
    this.user = user;
    this.sdkVersion = sdkVersion;
    this.profileId = profileId;
    this.gdprData = gdprData;
    this.slots = slots;
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
  public GdprData getGdprData() {
    return gdprData;
  }

  @NonNull
  public List<CdbRequestSlot> getSlots() {
    return slots;
  }

  @NonNull
  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(USER, user.toJson());
    json.put(PUBLISHER, publisher.toJson());
    json.put(SDK_VERSION, sdkVersion);
    json.put(PROFILE_ID, profileId);

    JSONArray jsonAdUnits = new JSONArray();
    for (CdbRequestSlot slot : slots) {
      jsonAdUnits.put(slot.toJson());
    }
    if (jsonAdUnits.length() > 0) {
      json.put(SLOTS, jsonAdUnits);
    }
    if (gdprData != null) {
      json.put(GDPR_CONSENT, gdprData.toJSONObject());
    }
    return json;
  }

  @NonNull
  @Override
  public String toString() {
    return "CdbRequest{" +
        "publisher=" + publisher +
        ", user=" + user +
        ", sdkVersion='" + sdkVersion + '\'' +
        ", profileId=" + profileId +
        ", gdprConsent=" + gdprData +
        ", slots=" + slots +
        '}';
  }
}
