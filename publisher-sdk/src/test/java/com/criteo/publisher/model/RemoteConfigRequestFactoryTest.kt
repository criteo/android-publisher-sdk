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
import com.criteo.publisher.integration.IntegrationRegistry
import com.criteo.publisher.util.BuildConfigWrapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class RemoteConfigRequestFactoryTest {

    @Rule
    @JvmField
    val mockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var buildConfigWrapper: BuildConfigWrapper

    @Mock
    private lateinit var integrationRegistry: IntegrationRegistry

    private lateinit var factory: RemoteConfigRequestFactory

    @Before
    fun setUp() {
        factory = RemoteConfigRequestFactory(
            context,
            "myCpId",
            null,
            buildConfigWrapper,
            integrationRegistry
        )
    }

    @Test
    fun createRequest_GivenInput_CreateRemoteConfigRequest() {
        buildConfigWrapper.stub {
            on { sdkVersion } doReturn "1.2.3"
        }

        integrationRegistry.stub {
            on { profileId } doReturn 456
        }

        context.stub {
            on { packageName } doReturn "my.bundle"
        }

        val request = factory.createRequest()

        assertThat(request.bundleId).isEqualTo("my.bundle")
        assertThat(request.criteoPublisherId).isEqualTo("myCpId")
        assertThat(request.sdkVersion).isEqualTo("1.2.3")
        assertThat(request.profileId).isEqualTo(456)
    }

    @Test
    fun createRequest_GivenInput_CreateRemoteConfigRequestWithInventoryGroupId() {
        buildConfigWrapper.stub {
            on { sdkVersion } doReturn "1.2.3"
        }

        integrationRegistry.stub {
            on { profileId } doReturn 456
        }

        context.stub {
            on { packageName } doReturn "my.bundle"
        }

        val factory = RemoteConfigRequestFactory(
            context,
            "myCpId",
            "myInventoryGroupId",
            buildConfigWrapper,
            integrationRegistry
        )
        val request = factory.createRequest()

        assertThat(request.bundleId).isEqualTo("my.bundle")
        assertThat(request.criteoPublisherId).isEqualTo("myCpId")
        assertThat(request.inventoryGroupId).isEqualTo("myInventoryGroupId")
        assertThat(request.sdkVersion).isEqualTo("1.2.3")
        assertThat(request.profileId).isEqualTo(456)
    }
}
