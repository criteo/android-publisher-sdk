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

plugins {
  id("com.android.library")
  kotlin("android")
  id("com.banno.gordon")
}

gordon.retryQuota.set(5)

androidLibModule()

android {
  defaultConfig {
    multiDexEnabled = true
  }

  packagingOptions {
    // Both AssertJ and ByteBuddy (via Mockito) brings this and the duplication yield an error
    exclude("META-INF/licenses/ASM")
  }
}

dependencies {
  implementation(project(":publisher-sdk"))
  implementation(project(":test-utils"))
  implementation(Deps.Kotlin.Stdlib)
  implementation(Deps.AndroidX.MultiDex)
  implementation(Deps.AndroidX.Annotations)
  implementation(Deps.AndroidX.RecyclerView)

  testImplementation(Deps.JUnit.JUnit)
  testImplementation(Deps.AssertJ.AssertJ)

  androidTestImplementation(Deps.AndroidX.SupportCoreUtils)
  androidTestImplementation(Deps.AndroidX.Test.Runner)
  androidTestImplementation(Deps.AndroidX.Test.Rules)
  androidTestImplementation(Deps.Mockito.Android)
  androidTestImplementation(Deps.Mockito.Kotlin)
  androidTestImplementation(Deps.AssertJ.AssertJ)
  androidTestImplementation(Deps.Square.Tape.Tape)
  androidTestImplementation(Deps.Google.AdMob)
  androidTestImplementation(Deps.GitHub.Kevinmost.JUnitRetryRule)

  androidTestImplementation(Deps.MoPub.Banner) {
    isTransitive = true
  }

  // Debug is needed because MoPub need some activities to be declare in the AndroidManifest.xml
  debugImplementation(Deps.MoPub.Interstitial) {
    isTransitive = true
  }
}
