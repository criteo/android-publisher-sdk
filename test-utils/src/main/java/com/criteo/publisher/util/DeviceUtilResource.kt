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

package com.criteo.publisher.util

import com.criteo.publisher.application.InstrumentationUtil
import com.criteo.publisher.mock.DependencyProviderRef
import com.criteo.publisher.mock.TestResource
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy

class DeviceUtilResource(private val dependencyProviderRef: DependencyProviderRef) : TestResource {

  override fun setUp() {
    if (!InstrumentationUtil.isRunningInInstrumentationTest()) {
      // The device util is checking that the SDK is supported by checking the Android API.
      // On Java tests, there is no Android API, and read value is 0 by default. Hence we override the decision to allow
      // Java tests to behave like supported SDK.
      val deviceUtil = spy(this.dependencyProviderRef.get().provideDeviceUtil())
      doReturn(true).`when`(deviceUtil).isVersionSupported
      this.dependencyProviderRef.get().inject(DeviceUtil::class.java, deviceUtil)
    }
  }
}
