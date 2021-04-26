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

package com.criteo.publisher.headerbidding

import com.criteo.publisher.mock.MockBean
import com.criteo.publisher.mock.MockedDependenciesRule
import com.criteo.publisher.util.AndroidUtil
import com.criteo.publisher.util.DeviceUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Dfp19HeaderBiddingRetroCompatTest {

  @Rule
  @JvmField
  val mockedDependenciesRule = MockedDependenciesRule()

  @MockBean
  private lateinit var androidUtil: AndroidUtil

  @MockBean
  private lateinit var deviceUtil: DeviceUtil

  private lateinit var handler: DfpHeaderBidding

  @Before
  fun setUp() {
    handler = DfpHeaderBidding(androidUtil, deviceUtil)
  }

  @Test
  fun test() {
    assertThat(handler.canHandle(this)).isFalse()
  }
}