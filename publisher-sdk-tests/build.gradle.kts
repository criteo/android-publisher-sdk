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
  id("io.gitlab.arturbosch.detekt")
}

androidLibModule()

dependencies {
  compileOnly(project(":publisher-sdk"))
  implementation(project(":test-utils"))
  implementation(Deps.Kotlin.Stdlib)
  implementation(Deps.AndroidX.Annotations)
  implementation(Deps.AndroidX.RecyclerView)

  // Needed because MoPub need some activities to be declare in the AndroidManifest.xml
  implementation(Deps.MoPub.Interstitial) {
    isTransitive = true
  }

  detektPlugins(Deps.Detekt.DetektFormatting)
}
