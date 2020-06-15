package com.criteo.publisher.model

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

    private lateinit var factory: RemoteConfigRequestFactory

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        factory = RemoteConfigRequestFactory(publisher, buildConfigWrapper)
    }

    @Test
    fun createRequest_GivenInput_CreateRemoteConfigRequest() {
        buildConfigWrapper.stub {
            on { sdkVersion } doReturn "1.2.3"
            on { profileId } doReturn 456
        }

        publisher.stub {
            on { bundleId } doReturn "my.bundle"
            on { criteoPublisherId } doReturn "myCpId"
        }

        val request = factory.createRequest()

        assertThat(request.bundleId).isEqualTo("my.bundle")
        assertThat(request.criteoPublisherId).isEqualTo("myCpId")
        assertThat(request.sdkVersion).isEqualTo("1.2.3")
        assertThat(request.profileId).isEqualTo(456)
    }
}
