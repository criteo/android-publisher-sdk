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
package com.criteo.publisher.model

import android.content.Context
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.integration.IntegrationRegistry
import com.criteo.publisher.util.AdvertisingInfo
import com.criteo.publisher.util.BuildConfigWrapper

@OpenForTesting
class RemoteConfigRequestFactory(
    private val context: Context,
    private val criteoPublisherId: String,
    private val buildConfigWrapper: BuildConfigWrapper,
    private val integrationRegistry: IntegrationRegistry,
    private val advertisingInfo: AdvertisingInfo
) {
  fun createRequest(): RemoteConfigRequest {
    return RemoteConfigRequest(
        criteoPublisherId,
        context.packageName,
        buildConfigWrapper.sdkVersion,
        integrationRegistry.profileId,
        advertisingInfo.advertisingId
    )
  }
}
