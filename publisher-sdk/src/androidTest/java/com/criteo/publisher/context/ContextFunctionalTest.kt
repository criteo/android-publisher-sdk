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

package com.criteo.publisher.context

import android.content.Context
import com.criteo.publisher.Criteo
import com.criteo.publisher.CriteoBannerView
import com.criteo.publisher.CriteoInterstitial
import com.criteo.publisher.CriteoUtil.givenInitializedCriteo
import com.criteo.publisher.TestAdUnits.BANNER_320_50
import com.criteo.publisher.TestAdUnits.INTERSTITIAL
import com.criteo.publisher.TestAdUnits.NATIVE
import com.criteo.publisher.advancednative.CriteoNativeLoader
import com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.mock.SpyBean
import com.criteo.publisher.network.PubSdkApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import javax.inject.Inject

@RunWith(Parameterized::class)
class ContextFunctionalTest(private val integration: TestedIntegration) {

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "{0}")
    fun data(): Collection<Array<out Any>> {
      return TestedIntegration.values().toList().map { arrayOf(it) }
    }
  }

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @Inject
  private lateinit var context: Context

  @SpyBean
  private lateinit var contextProvider: ContextProvider

  @SpyBean
  private lateinit var api: PubSdkApi

  @Test
  fun publisherExt_GivenContext_PutItInRequest() {
    val contextData = ContextData()
        .set(ContextData.CONTENT_URL, "https://www.criteo.com")
        .set("data.foo", "bar")
        .set("data.baz", 42)

    givenInitializedCriteo()
    integration.bid(this, contextData)

    verify(api).loadCdb(check {
      assertThat(it.publisher.ext).isEqualTo(
          mapOf(
              "content" to mapOf("url" to "https://www.criteo.com"),
              "data" to mapOf(
                  "foo" to "bar",
                  "baz" to 42L
              )
          )
      )
    }, any())
  }

  @Test
  fun userExt_GivenSomeUserContext_SdkInternallyFetchSomeContext() {
    val userData = UserData()
        .set(UserData.HASHED_EMAIL, EmailHasher.hash("john.doe@gmail.com"))
        .set(UserData.DEV_USER_ID, "abc123")
        .set("data.foo", listOf("bar", "baz"))
        .set("device.make", "ignored")

    doReturn(2).whenever(contextProvider).fetchDeviceConnectionType()
    doReturn("Apple").whenever(contextProvider).fetchDeviceMake()
    doReturn("iPhone X").whenever(contextProvider).fetchDeviceModel()
    doReturn(2048).whenever(contextProvider).fetchDeviceWidth()
    doReturn(2732).whenever(contextProvider).fetchDeviceHeight()
    doReturn(listOf("en", "he")).whenever(contextProvider).fetchUserLanguages()
    doReturn("FR").whenever(contextProvider).fetchUserCountry()
    doReturn("Portrait").whenever(contextProvider).fetchDeviceOrientation()
    doReturn(45).whenever(contextProvider).fetchSessionDuration()

    givenInitializedCriteo()
    Criteo.getInstance().setUserData(userData)
    integration.bid(this, ContextData())

    verify(api).loadCdb(check {
      assertThat(it.user.ext).isEqualTo(
          mapOf(
              "device" to mapOf(
                  "make" to "Apple",
                  "model" to "iPhone X",
                  "contype" to 2,
                  "w" to 2048,
                  "h" to 2732
              ),
              "data" to mapOf(
                  "orientation" to "Portrait",
                  "inputLanguage" to listOf("en", "he"),
                  "sessionDuration" to 45,
                  "hashedEmail" to "000e3171a5110c35c69d060112bd0ba55d9631c7c2ec93f1840e4570095b263a",
                  "devUserId" to "abc123",
                  "foo" to listOf("bar", "baz")
              ),
              "user" to mapOf(
                  "geo" to mapOf(
                      "country" to "FR"
                  )
              )
          )
      )
    }, any())
  }

  enum class TestedIntegration {
    STANDALONE_BANNER {
      override fun bid(test: ContextFunctionalTest, contextData: ContextData) {
        runOnMainThreadAndWait {
          CriteoBannerView(test.context, BANNER_320_50).loadAd(contextData)
        }
        test.mockedDependenciesRule.waitForIdleState()
      }
    },

    STANDALONE_INTERSTITIAL {
      override fun bid(test: ContextFunctionalTest, contextData: ContextData) {
        CriteoInterstitial(INTERSTITIAL).loadAd(contextData)
        test.mockedDependenciesRule.waitForIdleState()
      }
    },

    STANDALONE_NATIVE {
      override fun bid(test: ContextFunctionalTest, contextData: ContextData) {
        CriteoNativeLoader(NATIVE, mock(), mock()).loadAd(contextData)
        test.mockedDependenciesRule.waitForIdleState()
      }
    },

    // Represent InHouse and all AppBidding
    LOAD_BID {
      override fun bid(test: ContextFunctionalTest, contextData: ContextData) {
        Criteo.getInstance().loadBid(BANNER_320_50, contextData) {
          // load ad
        }
        test.mockedDependenciesRule.waitForIdleState()
      }
    };

    abstract fun bid(test: ContextFunctionalTest, contextData: ContextData)
  }

}
