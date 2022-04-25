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

package com.criteo.publisher.integration

import com.criteo.publisher.annotation.OpenForTesting

@OpenForTesting
class IntegrationDetector {

  fun isAdMobMediationPresent(): Boolean = AdMobMediationAdapterClass.isClassPresent()

  private fun String.isClassPresent(): Boolean {
    return try {
      // String classloader is the bootstrap class loader and cannot load user classes.
      // The classloader used to load this class is taken instead.
      val classLoader = IntegrationDetector::class.java.classLoader

      // Initialization of the class may takes time and is not required to check its existence.
      Class.forName(this, /* initialize */ false, classLoader)
      true
    } catch (expected: LinkageError) {
      false
    } catch (expected: ClassNotFoundException) {
      false
    }
  }

  private companion object {
    // Those class name are stable: they are used in publisher configuration on AdMob servers.
    // So renaming is clearly not expected.
    const val AdMobMediationAdapterClass = "com.criteo.mediation.google.CriteoAdapter"
  }
}
