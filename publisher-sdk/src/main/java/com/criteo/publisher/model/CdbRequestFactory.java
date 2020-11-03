/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.model;

import static com.criteo.publisher.util.TextUtils.getNotEmptyOrNullValue;

import android.content.Context;
import androidx.annotation.NonNull;
import com.criteo.publisher.bid.UniqueIdGenerator;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.util.AdvertisingInfo;
import com.criteo.publisher.util.BuildConfigWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

public class CdbRequestFactory {

  @NonNull
  private final Context context;

  @NonNull
  private final String criteoPublisherId;

  @NonNull
  private final DeviceInfo deviceInfo;

  @NonNull
  private final AdvertisingInfo advertisingInfo;

  @NonNull
  private final UserPrivacyUtil userPrivacyUtil;

  @NonNull
  private final UniqueIdGenerator uniqueIdGenerator;

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  @NonNull
  private final IntegrationRegistry integrationRegistry;

  public CdbRequestFactory(
      @NonNull Context context,
      @NonNull String criteoPublisherId,
      @NonNull DeviceInfo deviceInfo,
      @NonNull AdvertisingInfo advertisingInfo,
      @NonNull UserPrivacyUtil userPrivacyUtil,
      @NonNull UniqueIdGenerator uniqueIdGenerator,
      @NonNull BuildConfigWrapper buildConfigWrapper,
      @NonNull IntegrationRegistry integrationRegistry
  ) {
    this.context = context;
    this.criteoPublisherId = criteoPublisherId;
    this.deviceInfo = deviceInfo;
    this.advertisingInfo = advertisingInfo;
    this.userPrivacyUtil = userPrivacyUtil;
    this.uniqueIdGenerator = uniqueIdGenerator;
    this.buildConfigWrapper = buildConfigWrapper;
    this.integrationRegistry = integrationRegistry;
  }

  @NonNull
  public CdbRequest createRequest(
      @NonNull List<CacheAdUnit> requestedAdUnits,
      @SuppressWarnings("unused") // TODO EE-1321
      @NonNull ContextData contextData
  ) {
    Publisher publisher = Publisher.create(
        context.getPackageName(),
        criteoPublisherId,
        new HashMap<>() // TODO EE-1321
    );

    User user = User.create(
        advertisingInfo.getAdvertisingId(),
        getNotEmptyOrNullValue(userPrivacyUtil.getMopubConsent()),
        getNotEmptyOrNullValue(userPrivacyUtil.getIabUsPrivacyString()),
        getNotEmptyOrNullValue(userPrivacyUtil.getUsPrivacyOptout()),
        new HashMap<>() // TODO EE-1321
    );

    return CdbRequest.create(
        uniqueIdGenerator.generateId(),
        publisher,
        user,
        buildConfigWrapper.getSdkVersion(),
        integrationRegistry.getProfileId(),
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
