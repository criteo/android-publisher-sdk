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
  jacoco
  kotlin("android")
  id("io.gitlab.arturbosch.detekt")
}

androidLibModule()

android {
  defaultConfig {
    multiDexEnabled = true
  }

  packagingOptions {
    // Both AssertJ and ByteBuddy (via Mockito) bring this and the duplication yields an error
    exclude("META-INF/licenses/ASM")
  }
}



dependencies {
  implementation(project(":publisher-sdk"))
  implementation(project(":test-utils"))
  implementation(Deps.Kotlin.Stdlib)
  implementation(Deps.Google.AdMob19)

  androidTestImplementation(Deps.AndroidX.Test.Runner)
  androidTestImplementation(Deps.AssertJ.AssertJ)
  androidTestImplementation(Deps.Mockito.Android)

  detektPlugins(Deps.Detekt.DetektFormatting)
}