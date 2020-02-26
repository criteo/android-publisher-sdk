package com.criteo.publisher.model

import com.criteo.publisher.Util.DeviceUtil
import com.criteo.publisher.privacy.UserPrivacyUtil
import com.criteo.publisher.privacy.gdpr.GdprData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.concurrent.Future

class CdbRequestFactoryTest {

    private val SDK_PROFILE_ID = 235

    @Mock
    private lateinit var user: User

    @Mock
    private lateinit var publisher: Publisher

    @Mock
    private lateinit var deviceInfo: DeviceInfo

    @Mock
    private lateinit var deviceUtil: DeviceUtil

    @Mock
    private lateinit var userPrivacyUtil: UserPrivacyUtil

    private lateinit var factory: CdbRequestFactory

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        factory = CdbRequestFactory(
                user,
                publisher,
                deviceInfo,
                deviceUtil,
                userPrivacyUtil
        )
    }

    @Test
    fun userAgent_GivenDeviceInfo_DelegateToIt() {
        val expected: Future<String> = mock()
        whenever(deviceInfo.userAgent).thenReturn(expected)

        val userAgent = factory.userAgent

        assertThat(userAgent).isSameAs(expected)
    }

    @Test
    fun createRequest_GivenNonEmptyDeviceId_SetItInUser() {
        whenever(deviceUtil.advertisingId).thenReturn("myId")

        factory.createRequest(emptyList())

        verify(user).setDeviceId("myId")
    }

    @Test
    fun createRequest_GivenNullDeviceId_DoesNotSetItInUser() {
        whenever(deviceUtil.advertisingId).thenReturn(null)

        factory.createRequest(emptyList())

        verify(user, never()).setDeviceId(any())
    }

    @Test
    fun createRequest_GivenEmptyDeviceId_DoNotSetItInUser() {
        whenever(deviceUtil.advertisingId).thenReturn("")

        factory.createRequest(emptyList())

        verify(user, never()).setDeviceId(any())
    }

    @Test
    fun createRequest_GivenNonEmptyUsPrivacyString_SetItInUser() {
        whenever(userPrivacyUtil.iabUsPrivacyString).thenReturn("myPrivacy")

        factory.createRequest(emptyList())

        verify(user).uspIab = "myPrivacy"
    }

    @Test
    fun createRequest_GivenEmptyUsPrivacyString_DoNotSetItInUser() {
        whenever(userPrivacyUtil.iabUsPrivacyString).thenReturn("")

        factory.createRequest(emptyList())

        verify(user, never()).uspIab = any()
    }

    @Test
    fun createRequest_GivenNonEmptyUsPrivacyOptOut_SetItInUser() {
        whenever(userPrivacyUtil.usPrivacyOptout).thenReturn("myPrivacy")

        factory.createRequest(emptyList())

        verify(user).uspOptout = "myPrivacy"
    }

    @Test
    fun createRequest_GivenEmptyUsPrivacyOptOut_DoNotSetItInUser() {
        whenever(userPrivacyUtil.usPrivacyOptout).thenReturn("")
        factory.createRequest(emptyList())
        verify(user, never()).uspOptout = any()
    }

    @Test
    fun createRequest_GivenNonEmptyMoPubConsent_SetItInUser() {
        whenever(userPrivacyUtil.mopubConsent).thenReturn("myPrivacy")

        factory.createRequest(emptyList())

        verify(user).mopubConsent = "myPrivacy"
    }

    @Test
    fun createRequest_GivenEmptyMoPubConsent_DoNotSetItInUser() {
        whenever(userPrivacyUtil.mopubConsent).thenReturn("")

        factory.createRequest(emptyList())

        verify(user, never()).mopubConsent = any()
    }

    @Test
    fun createRequest_GivenInput_BuildRequest() {
        val adUnits: List<CacheAdUnit> = mock()
        val expectedGdpr: GdprData = mock()

        whenever(user.sdkVersion).thenReturn("1.2.3")
        whenever(userPrivacyUtil.gdprData).thenReturn(expectedGdpr)

        val request = factory.createRequest(adUnits)

        assertThat(request.user).isEqualTo(user)
        assertThat(request.publisher).isEqualTo(publisher)
        assertThat(request.sdkVersion).isEqualTo("1.2.3")
        assertThat(request.profileId).isEqualTo(SDK_PROFILE_ID)
        assertThat(request.gdprData).isEqualTo(expectedGdpr)
        assertThat(request.adUnits).isEqualTo(adUnits)
    }

}