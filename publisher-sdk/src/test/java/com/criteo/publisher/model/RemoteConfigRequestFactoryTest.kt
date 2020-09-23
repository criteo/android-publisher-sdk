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

import com.criteo.publisher.integration.IntegrationRegistry
import com.criteo.publisher.util.AdvertisingInfo
import com.criteo.publisher.util.BuildConfigWrapper
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class RemoteConfigRequestFactoryTest {

    @Mock
    private lateinit var publisher: Publisher

    @Mock
    private lateinit var buildConfigWrapper: BuildConfigWrapper

    @Mock
    private lateinit var integrationRegistry: IntegrationRegistry

    @Mock
    private lateinit var advertisingInfo: AdvertisingInfo

    private lateinit var factory: RemoteConfigRequestFactory

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        factory = RemoteConfigRequestFactory(publisher, buildConfigWrapper, integrationRegistry, advertisingInfo)
    }

    @Test
    fun createRequest_GivenInput_CreateRemoteConfigRequest() {
        buildConfigWrapper.stub {
            on { sdkVersion } doReturn "1.2.3"
        }

        integrationRegistry.stub {
            on { profileId } doReturn 456
        }

        publisher.stub {
            on { bundleId } doReturn "my.bundle"
            on { criteoPublisherId } doReturn "myCpId"
        }

        advertisingInfo.stub {
            on { advertisingId } doReturn "myAdvertisingId"
        }

        val request = factory.createRequest()

        assertThat(request.bundleId).isEqualTo("my.bundle")
        assertThat(request.criteoPublisherId).isEqualTo("myCpId")
        assertThat(request.sdkVersion).isEqualTo("1.2.3")
        assertThat(request.profileId).isEqualTo(456)
        assertThat(request.deviceId).isEqualTo("myAdvertisingId")
    }
}
