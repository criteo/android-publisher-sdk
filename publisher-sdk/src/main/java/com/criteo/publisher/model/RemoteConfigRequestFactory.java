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

import android.content.Context;
import androidx.annotation.NonNull;
import com.criteo.publisher.integration.IntegrationRegistry;
import com.criteo.publisher.util.AdvertisingInfo;
import com.criteo.publisher.util.BuildConfigWrapper;

public class RemoteConfigRequestFactory {

  @NonNull
  private final Context context;

  @NonNull
  private final String criteoPublisherId;

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  @NonNull
  private final IntegrationRegistry integrationRegistry;

  @NonNull
  private final AdvertisingInfo advertisingInfo;

  public RemoteConfigRequestFactory(
      @NonNull Context context,
      @NonNull String criteoPublisherId,
      @NonNull BuildConfigWrapper buildConfigWrapper,
      @NonNull IntegrationRegistry integrationRegistry,
      @NonNull AdvertisingInfo advertisingInfo
  ) {
    this.context = context;
    this.criteoPublisherId = criteoPublisherId;
    this.buildConfigWrapper = buildConfigWrapper;
    this.integrationRegistry = integrationRegistry;
    this.advertisingInfo = advertisingInfo;
  }

  @NonNull
  public RemoteConfigRequest createRequest() {
    return RemoteConfigRequest.create(
        criteoPublisherId,
        context.getPackageName(),
        buildConfigWrapper.getSdkVersion(),
        integrationRegistry.getProfileId(),
        advertisingInfo.getAdvertisingId()
    );
  }
}
