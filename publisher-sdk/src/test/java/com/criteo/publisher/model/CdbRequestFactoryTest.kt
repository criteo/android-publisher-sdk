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
import com.criteo.publisher.bid.UniqueIdGenerator
import com.criteo.publisher.context.ContextData
import com.criteo.publisher.context.ContextProvider
import com.criteo.publisher.context.UserData
import com.criteo.publisher.context.UserDataHolder
import com.criteo.publisher.integration.IntegrationRegistry
import com.criteo.publisher.privacy.UserPrivacyUtil
import com.criteo.publisher.privacy.gdpr.GdprData
import com.criteo.publisher.util.AdUnitType.CRITEO_BANNER
import com.criteo.publisher.util.AdvertisingInfo
import com.criteo.publisher.util.BuildConfigWrapper
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

class CdbRequestFactoryTest {

  @Mock
  private lateinit var deviceInfo: DeviceInfo

  @Mock
  private lateinit var advertisingInfo: AdvertisingInfo

  @Mock
  private lateinit var userPrivacyUtil: UserPrivacyUtil

  @Mock
  private lateinit var uniqueIdGenerator: UniqueIdGenerator

  @Mock
  private lateinit var buildConfigWrapper: BuildConfigWrapper

  @Mock
  private lateinit var integrationRegistry: IntegrationRegistry

  @Mock
  private lateinit var context: Context

  @Mock
  private lateinit var contextProvider: ContextProvider

  private val userDataHolder = UserDataHolder()

  private val cpId = "myCpId"

  private lateinit var factory: CdbRequestFactory

  private val adUnitId = AtomicInteger()

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    whenever(userPrivacyUtil.mopubConsent).thenReturn("mopubConsent")
    whenever(userPrivacyUtil.iabUsPrivacyString).thenReturn("iabUsPrivacyString")
    whenever(userPrivacyUtil.usPrivacyOptout).thenReturn("usPrivacyoptout")

    factory = CdbRequestFactory(
        context,
        cpId,
        deviceInfo,
        advertisingInfo,
        userPrivacyUtil,
        uniqueIdGenerator,
        buildConfigWrapper,
        integrationRegistry,
        contextProvider,
        userDataHolder
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
  fun createRequest_GivenInput_BuildRequest() {
    val adUnit = createAdUnit()
    val adUnits: List<CacheAdUnit> = listOf(adUnit)
    val contextData: ContextData = ContextData().set("a.a", "foo").set("b", "bar")
    val expectedGdpr: GdprData = mock()

    val expectedSlot = CdbRequestSlot.create(
        "impId",
        adUnit.placementId,
        adUnit.adUnitType,
        adUnit.size
    )

    buildConfigWrapper.stub {
      on { sdkVersion } doReturn "1.2.3"
    }

    whenever(context.packageName).thenReturn("bundle.id")
    whenever(integrationRegistry.profileId).thenReturn(42)
    whenever(userPrivacyUtil.gdprData).thenReturn(expectedGdpr)
    whenever(uniqueIdGenerator.generateId())
        .thenReturn("myRequestId")
        .thenReturn("impId")

    whenever(contextProvider.fetchUserContext()).thenReturn(
        mapOf(
            "a" to "1",
            "b.a" to "2"
        )
    )
    userDataHolder.set(
        UserData()
            .set("a", "skipped")
            .set("b.b", "3")
    )

    val expectedPublisher = Publisher.create(
        "bundle.id",
        "myCpId",
        mapOf(
            "a" to mapOf("a" to "foo"),
            "b" to "bar"
        )
    )

    val expectedUserExt = mapOf(
        "a" to "1",
        "b" to mapOf(
            "a" to "2",
            "b" to "3"
        )
    )

    val request = factory.createRequest(adUnits, contextData)

    assertThat(request.id).isEqualTo("myRequestId")
    assertThat(request.publisher).isEqualTo(expectedPublisher)
    assertThat(request.user.ext()).isEqualTo(expectedUserExt)
    assertThat(request.sdkVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(42)
    assertThat(request.gdprData).isEqualTo(expectedGdpr)
    assertThat(request.slots).containsExactlyInAnyOrder(expectedSlot)
  }

  @Test
  fun givenOneRequestWithNonEmptyPrivacyValue_AndOneRequestWithEmptyPrivacyValues_VerifyRequestsAreDifferent() {
    // request 1
    val adUnit = createAdUnit()
    val adUnits: List<CacheAdUnit> = listOf(adUnit)
    val contextData = ContextData()
    val expectedGdpr: GdprData = mock()

    val expectedSlot = CdbRequestSlot.create(
        "impId",
        adUnit.placementId,
        adUnit.adUnitType,
        adUnit.size
    )

    buildConfigWrapper.stub {
      on { sdkVersion } doReturn "1.2.3"
    }

    userPrivacyUtil.stub {
      on { gdprData } doReturn expectedGdpr
      on { usPrivacyOptout } doReturn "usPrivacyOptout"
      on { iabUsPrivacyString } doReturn "iabUsPrivacyString"
      on { mopubConsent } doReturn "mopubConsent"
    }

    whenever(context.packageName).thenReturn("bundle.id")
    whenever(integrationRegistry.profileId).thenReturn(1337)
    whenever(uniqueIdGenerator.generateId())
        .thenReturn("myRequestId")
        .thenReturn("impId")

    var request = factory.createRequest(adUnits, contextData)

    assertThat(request.id).isEqualTo("myRequestId")
    assertThat(request.publisher).isEqualTo(Publisher.create("bundle.id", "myCpId", mapOf()))
    assertThat(request.sdkVersion).isEqualTo("1.2.3")
    assertThat(request.profileId).isEqualTo(1337)
    assertThat(request.gdprData).isEqualTo(expectedGdpr)
    assertThat(request.user.uspIab()).isEqualTo("iabUsPrivacyString")
    assertThat(request.user.uspOptout()).isEqualTo("usPrivacyOptout")
    assertThat(request.user.mopubConsent()).isEqualTo("mopubConsent")
    assertThat(request.slots).containsExactlyInAnyOrder(expectedSlot)

    // request 2
    userPrivacyUtil.stub {
      on { usPrivacyOptout } doReturn ""
      on { iabUsPrivacyString } doReturn ""
      on { mopubConsent } doReturn ""
    }

    request = factory.createRequest(adUnits, contextData)

    assertThat(request.user.uspIab()).isNull()
    assertThat(request.user.uspOptout()).isNull()
    assertThat(request.user.mopubConsent()).isNull()
  }

  @Test
  fun createRequest_GivenAdUnits_MapThemToRequestSlotWithImpressionId() {
    val adUnit1 = createAdUnit()
    val adUnit2 = createAdUnit()
    val adUnits: List<CacheAdUnit> = listOf(adUnit1, adUnit2)
    val contextData: ContextData = mock()

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
      on { generateId() }.doReturn("myRequestId", "impId1", "impId2")
    }

    buildConfigWrapper.stub {
      on { sdkVersion } doReturn "1.2.3"
    }

    whenever(context.packageName).thenReturn("bundle.id")
    whenever(integrationRegistry.profileId).thenReturn(1337)

    val request = factory.createRequest(adUnits, contextData)

    assertThat(request.slots).containsExactlyInAnyOrder(expectedSlot1, expectedSlot2)
  }

  @Test
  fun mergeToNestedMap_GivenNoMap_ReturnEmpty() {
    val nestedMap = factory.mergeToNestedMap()

    assertThat(nestedMap).isEmpty()
  }

  @Test
  fun mergeToNestedMap_GivenMultipleMapsWithOverride_ReturnMergedAndNestedMap() {
    val map1 = mapOf(
        "a.a.a" to 1337,
        "a.c.b" to "...",
        "a.a" to "skipped",
        "a.a.a.a" to "skipped"
    )

    val map2 = mapOf(
        "a.a.a" to 3,
        "a.b" to "foo",
        "a.c.a" to listOf("foo", "bar"),
        "a" to "skipped",
        ".a" to "skipped",
        "a.c.c." to "skipped",
        "a..c.d" to "skipped",
        "a.c.e" to mapOf("valueMap" to mapOf("a" to "map as value")),
        "a.c.e.valueMap.b" to "skipped"
    )

    val expectedMap = mapOf(
        "a" to mapOf(
            "a" to mapOf(
                "a" to 1337
            ),
            "c" to mapOf(
                "b" to "...",
                "a" to listOf("foo", "bar"),
                "e" to mapOf("valueMap" to mapOf("a" to "map as value"))
            ),
            "b" to "foo"
        )
    )

    val nestedMap = factory.mergeToNestedMap(map1, map2)

    assertThat(nestedMap).isEqualTo(expectedMap)
  }

  private fun createAdUnit(): CacheAdUnit {
    val id = "adUnit #" + adUnitId.incrementAndGet()
    return CacheAdUnit(AdSize(1, 2), id, CRITEO_BANNER)
  }
}
