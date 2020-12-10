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

package com.dummypublisher

import com.criteo.publisher.logging.PublisherCodeRemoverTest

object DummyPublisherCode {

  const val secrets = "secrets"

  fun sdkDummyInterfaceThrowingGenericException() = sdkDummyInterfaceCalling {
    throw IllegalArgumentException(secrets)
  }

  fun sdkDummyInterfaceCallingSdkCode(sdkCode: () -> Unit) = sdkDummyInterfaceCalling(sdkCode)

  private fun sdkDummyInterfaceCalling(code: () -> Unit): PublisherCodeRemoverTest.SdkDummyInterface {
    return object : PublisherCodeRemoverTest.SdkDummyInterface {
      override fun foo() {
        addDummyStacks {
          code()
        }
      }
    }
  }

  private fun addDummyStacks(nbStack: Int = 5, thenAction: () -> Unit) {
    if (nbStack <= 1) {
      thenAction()
    } else {
      addDummyStacks(nbStack - 1, thenAction)
    }
  }
}
