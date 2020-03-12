package com.criteo.publisher.model;

import static com.criteo.publisher.Util.TextUtils.isEmpty;

import android.support.annotation.NonNull;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.bid.UniqueIdGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class CdbRequestFactory {

  /**
   * Profile ID used by the SDK, so CDB and the Supply chain can recognize that the request come
   * from the PublisherSDK.
   */
  private static final int PROFILE_ID = 235;

  @NonNull
  private final User user;

  @NonNull
  private final Publisher publisher;

  @NonNull
  private final DeviceInfo deviceInfo;

  @NonNull
  private final DeviceUtil deviceUtil;

  @NonNull
  private final UserPrivacyUtil userPrivacyUtil;

  @NonNull
  private final UniqueIdGenerator uniqueIdGenerator;

  public CdbRequestFactory(
      @NonNull User user,
      @NonNull Publisher publisher,
      @NonNull DeviceInfo deviceInfo,
      @NonNull DeviceUtil deviceUtil,
      @NonNull UserPrivacyUtil userPrivacyUtil,
      @NonNull UniqueIdGenerator uniqueIdGenerator) {
    this.user = user;
    this.publisher = publisher;
    this.deviceInfo = deviceInfo;
    this.deviceUtil = deviceUtil;
    this.userPrivacyUtil = userPrivacyUtil;
    this.uniqueIdGenerator = uniqueIdGenerator;
  }

  @NonNull
  public CdbRequest createRequest(List<CacheAdUnit> requestedAdUnits) {
    String advertisingId = deviceUtil.getAdvertisingId();
    if (!isEmpty(advertisingId)) {
      user.setDeviceId(advertisingId);
    }

    String uspIab = userPrivacyUtil.getIabUsPrivacyString();
    if (!isEmpty(uspIab)) {
      user.setUspIab(uspIab);
    }

    String uspOptout = userPrivacyUtil.getUsPrivacyOptout();
    if (!isEmpty(uspOptout)) {
      user.setUspOptout(uspOptout);
    }

    String mopubConsent = userPrivacyUtil.getMopubConsent();
    if (!isEmpty(mopubConsent)) {
      user.setMopubConsent(mopubConsent);
    }

    return new CdbRequest(
        publisher,
        user,
        user.getSdkVersion(),
        PROFILE_ID,
        userPrivacyUtil.getGdprData(),
        createRequestSlots(requestedAdUnits)
    );
  }

  @NonNull
  private List<CdbRequestSlot> createRequestSlots(List<CacheAdUnit> requestedAdUnits) {
    List<CdbRequestSlot> slots = new ArrayList<>();
    for (CacheAdUnit requestedAdUnit : requestedAdUnits) {
      slots.add(createRequestSlot(requestedAdUnit));
    }
    return slots;
  }

  @NonNull
  private CdbRequestSlot createRequestSlot(CacheAdUnit requestedAdUnit) {
    return CdbRequestSlot.create(
        uniqueIdGenerator.generateId(),
        requestedAdUnit.getPlacementId(),
        requestedAdUnit.getAdUnitType(),
        requestedAdUnit.getSize()
    );
  }

  @NonNull
  public Future<String> getUserAgent() {
    return deviceInfo.getUserAgent();
  }

}
