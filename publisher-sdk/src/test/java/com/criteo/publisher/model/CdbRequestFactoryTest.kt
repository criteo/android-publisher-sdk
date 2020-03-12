package com.criteo.publisher.model

import com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER
import com.criteo.publisher.Util.DeviceUtil
import com.criteo.publisher.bid.UniqueIdGenerator
import com.nhaarman.mockitokotlin2.doReturn
import com.criteo.publisher.privacy.UserPrivacyUtil
import com.criteo.publisher.privacy.gdpr.GdprData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
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
import java.util.concurrent.atomic.AtomicInteger

class CdbRequestFactoryTest {

    private companion object {
        const val SDK_PROFILE_ID = 235
    }

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

    @Mock
    private lateinit var uniqueIdGenerator: UniqueIdGenerator

    private lateinit var factory: CdbRequestFactory

    private val adUnitId = AtomicInteger()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        factory = CdbRequestFactory(
                user,
                publisher,
                deviceInfo,
                deviceUtil,
                userPrivacyUtil,
                uniqueIdGenerator
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
        val adUnit = createAdUnit()
        val adUnits: List<CacheAdUnit> = listOf(adUnit)
        val expectedGdpr: GdprData = mock()

        val expectedSlot = CdbRequestSlot.create(
            "impId",
            adUnit.placementId,
            adUnit.adUnitType,
            adUnit.size
        )

        whenever(user.sdkVersion).thenReturn("1.2.3")
        whenever(userPrivacyUtil.gdprData).thenReturn(expectedGdpr)
        whenever(uniqueIdGenerator.generateId()).thenReturn("impId")

        val request = factory.createRequest(adUnits)

        assertThat(request.user).isEqualTo(user)
        assertThat(request.publisher).isEqualTo(publisher)
        assertThat(request.sdkVersion).isEqualTo("1.2.3")
        assertThat(request.profileId).isEqualTo(SDK_PROFILE_ID)
        assertThat(request.gdprData).isEqualTo(expectedGdpr)
        assertThat(request.slots).containsExactlyInAnyOrder(expectedSlot)
    }

    @Test
    fun createRequest_GivenAdUnits_MapThemToRequestSlotWithImpressionId() {
        val adUnit1 = createAdUnit()
        val adUnit2 = createAdUnit()
        val adUnits: List<CacheAdUnit> = listOf(adUnit1, adUnit2)

        val expectedSlot1 = CdbRequestSlot.create(
            "impId1",
            adUnit1.placementId,
            adUnit1.adUnitType,
            adUnit1.size
        )

        val expectedSlot2 = CdbRequestSlot.create(
            "impId2",
            adUnit2.placementId,
            adUnit2.adUnitType,
            adUnit2.size
        )

        uniqueIdGenerator.stub {
            on { generateId() }.doReturn("impId1", "impId2")
        }

        val request = factory.createRequest(adUnits)

        assertThat(request.slots).containsExactlyInAnyOrder(expectedSlot1, expectedSlot2)
    }

    private fun createAdUnit(): CacheAdUnit {
        val id = "adUnit #" + adUnitId.incrementAndGet()
        return CacheAdUnit(AdSize(1, 2), id, CRITEO_BANNER)
    }

}