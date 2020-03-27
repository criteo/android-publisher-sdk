package com.criteo.publisher.model;

import static com.criteo.publisher.Util.TextUtils.getNotEmptyOrNullValue;

import android.support.annotation.NonNull;
import com.criteo.publisher.Util.BuildConfigWrapper;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.bid.UniqueIdGenerator;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class CdbRequestFactory {

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

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  public CdbRequestFactory(
      @NonNull Publisher publisher,
      @NonNull DeviceInfo deviceInfo,
      @NonNull DeviceUtil deviceUtil,
      @NonNull UserPrivacyUtil userPrivacyUtil,
      @NonNull UniqueIdGenerator uniqueIdGenerator,
      @NonNull BuildConfigWrapper buildConfigWrapper
  ) {
    this.publisher = publisher;
    this.deviceInfo = deviceInfo;
    this.deviceUtil = deviceUtil;
    this.userPrivacyUtil = userPrivacyUtil;
    this.uniqueIdGenerator = uniqueIdGenerator;
    this.buildConfigWrapper = buildConfigWrapper;
  }

  @NonNull
  public CdbRequest createRequest(List<CacheAdUnit> requestedAdUnits) {
    User user = User.create(
        deviceUtil.getAdvertisingId(),
        getNotEmptyOrNullValue(userPrivacyUtil.getMopubConsent()),
        getNotEmptyOrNullValue(userPrivacyUtil.getIabUsPrivacyString()),
        getNotEmptyOrNullValue(userPrivacyUtil.getUsPrivacyOptout())
    );

    return new CdbRequest(
        publisher,
        user,
        buildConfigWrapper.getSdkVersion(),
        buildConfigWrapper.getProfileId(),
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
