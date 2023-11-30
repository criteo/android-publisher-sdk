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

import static com.criteo.publisher.context.ContextUtil.toMap;
import static com.criteo.publisher.util.TextUtils.getNotEmptyOrNullValue;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.bid.UniqueIdGenerator;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.context.ContextProvider;
import com.criteo.publisher.context.UserDataHolder;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.privacy.UserPrivacyUtil;
import com.criteo.publisher.util.AdvertisingInfo;
import com.criteo.publisher.util.BuildConfigWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

  @NonNull
  private final ContextProvider contextProvider;

  @NonNull
  private final UserDataHolder userDataHolder;

  @NonNull
  private final Config config;

  public CdbRequestFactory(
      @NonNull Context context,
      @NonNull String criteoPublisherId,
      @NonNull DeviceInfo deviceInfo,
      @NonNull AdvertisingInfo advertisingInfo,
      @NonNull UserPrivacyUtil userPrivacyUtil,
      @NonNull UniqueIdGenerator uniqueIdGenerator,
      @NonNull BuildConfigWrapper buildConfigWrapper,
      @NonNull IntegrationRegistry integrationRegistry,
      @NonNull ContextProvider contextProvider,
      @NonNull UserDataHolder userDataHolder,
      @NonNull Config config
  ) {
    this.context = context;
    this.criteoPublisherId = criteoPublisherId;
    this.deviceInfo = deviceInfo;
    this.advertisingInfo = advertisingInfo;
    this.userPrivacyUtil = userPrivacyUtil;
    this.uniqueIdGenerator = uniqueIdGenerator;
    this.buildConfigWrapper = buildConfigWrapper;
    this.integrationRegistry = integrationRegistry;
    this.contextProvider = contextProvider;
    this.userDataHolder = userDataHolder;
    this.config = config;
  }

  @NonNull
  public CdbRequest createRequest(
      @NonNull List<CacheAdUnit> requestedAdUnits,
      @NonNull ContextData contextData
  ) {
    Map<String, Object> publisherExt = mergeToNestedMap(toMap(contextData));

    Publisher publisher = new Publisher(
        context.getPackageName(),
        criteoPublisherId,
        publisherExt
    );

    Map<String, Object> userExt = mergeToNestedMap(
        contextProvider.fetchUserContext(),
        toMap(userDataHolder.get())
    );

    User user = new User(
        advertisingInfo.getAdvertisingId(),
        getNotEmptyOrNullValue(userPrivacyUtil.getIabUsPrivacyString()),
        getNotEmptyOrNullValue(userPrivacyUtil.getUsPrivacyOptout()),
        userExt
    );

    return new CdbRequest(
        uniqueIdGenerator.generateId(),
        publisher,
        user,
        buildConfigWrapper.getSdkVersion(),
        integrationRegistry.getProfileId(),
        userPrivacyUtil.getGdprData(),
        createRequestSlots(requestedAdUnits),
        createRegs()
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
    return new CdbRequestSlot(
        uniqueIdGenerator.generateId(),
        requestedAdUnit.getPlacementId(),
        requestedAdUnit.getAdUnitType(),
        requestedAdUnit.getSize(),
        getSupportedApiFrameworkList()
    );
  }

  @NonNull
  public Future<String> getUserAgent() {
    return deviceInfo.getUserAgent();
  }

  /**
   * Transform given maps in a nested structure and merge them
   * <p>
   * The keys of the maps determine the nested structure. A "." (dot) represent a nested level, such as dot notation in
   * Java. For instance the entry <code>"a.b.c" -> "value"</code> represents <code>{a: {b: { c: "value"}}}</code>
   * <p>
   * The merge keeps the first elements and drops the next elements with the same key or with an incompatible structure.
   * The merge policy uses the iteration order of the keys and the order of given maps. So it is recommended to give map
   * with a deterministic iteration order such as {@link java.util.SortedMap} or {@link java.util.LinkedHashMap}. Here
   * is an example of merge:
   * <pre><code>
   *   // First map
   *   a.a.a = 1337 // (1)
   *   a.c.b = "..."
   *   a.a = 1 // Skipped because a.a is defined as a node at (1)
   *   a.a.a.a = 2 // Skipped because a.a.a is defined as a leaf at (1)
   *
   *   // Second map
   *   a.a.a = 42 // Skipped because a.a.a is already defined at (1)
   *   a.b = "foo"
   *   a.c.a = ["foo", "bar"]
   *
   *   // Gives
   *   {
   *     a: {
   *       a: {
   *         a: 1337
   *       },
   *       c: {
   *         b: "...",
   *         a: ["foo", "bar"]
   *       }
   *     },
   *     b: "foo"
   *   }
   * </code></pre>
   *
   * @param flattenMaps maps to merge into a nested structure
   * @return nested structure
   */
  @SuppressWarnings({"SuspiciousMethodCalls", "ConstantConditions", "unchecked"})
  @NonNull
  @SafeVarargs
  @VisibleForTesting
  public final Map<String, Object> mergeToNestedMap(Map<String, Object>... flattenMaps) {
    Map<String, Object> nestedMap = new LinkedHashMap<>();
    Set<Map<String, Object>> subNodes = Collections.newSetFromMap(new IdentityHashMap<>());

    for (Map<String, Object> flattenMap : flattenMaps) {
      for (Entry<String, Object> entry : flattenMap.entrySet()) {
        Map<String, Object> node = nestedMap;

        String[] pathParts = entry.getKey().split("\\.", -1);
        if (isNotValid(pathParts)) {
          continue;
        }

        // Go or create nested structure until last path part
        for (int i = 0; i < pathParts.length - 1; i++) {
          String pathPart = pathParts[i];

          if (node.containsKey(pathPart)) {
            Object nestedValue = node.get(pathPart);
            if (subNodes.contains(nestedValue)) {
              // It's a sub node, go deeper
              // safe because only Map<String, Object> are put in subNodes
              node = (Map<String, Object>) nestedValue;
            } else {
              // It's a leaf, abort
              break;
            }
          } else {
            // Create a new node and go deeper
            Map<String, Object> newNode = new LinkedHashMap<>();
            subNodes.add(newNode);
            node.put(pathPart, newNode);
            node = newNode;
          }
        }

        String lastPathPart = pathParts[pathParts.length - 1];
        if (!node.containsKey(lastPathPart)) {
          // If value is already there, abort
          node.put(lastPathPart, entry.getValue());
        }
      }
    }

    return nestedMap;
  }

  private boolean isNotValid(String[] pathParts) {
    for (String pathPart : pathParts) {
      if (pathPart.isEmpty()) {
        // Reject empty part
        return true;
      }
    }
    return false;
  }

  @Nullable
  private CdbRegs createRegs() {
    Boolean tagForChildTreatment = userPrivacyUtil.getTagForChildDirectedTreatment();
    return tagForChildTreatment == null ? null : new CdbRegs(tagForChildTreatment);
  }

  @NonNull
  private List<ApiFramework> getSupportedApiFrameworkList() {
    List<ApiFramework> supportedApiFrameworkList = new ArrayList<>();
    if (config.isMraidEnabled()) {
      supportedApiFrameworkList.add(ApiFramework.MRAID_1);
    }
    if (config.isMraid2Enabled()) {
      supportedApiFrameworkList.add(ApiFramework.MRAID_2);
    }
    return supportedApiFrameworkList;
  }
}
