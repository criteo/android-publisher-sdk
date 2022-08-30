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
  `maven-publish`
  signing
  jacoco
  kotlin("android")
  id("kotlin-allopen")
  id("com.vanniktech.android.javadoc") version "0.3.0"
  id("io.gitlab.arturbosch.detekt")
}

allOpen {
  annotation("com.criteo.publisher.annotation.OpenForTesting")
}

androidLibModule()

android {
  defaultConfig {
    multiDexEnabled = true
  }
}

dependencies {
  compileOnly(project(":publisher-sdk"))
  compileOnly(Deps.AssertJ.AssertJ)

  implementation(Deps.AndroidX.MultiDex)
  implementation(Deps.JUnit.JUnit)
  implementation(Deps.Square.OkHttp.MockWebServer)
  implementation(Deps.Square.OkHttp.OkHttpTls)
  compileOnly(Deps.Square.Moshi.Adapter)

  compileOnly(Deps.Mockito.Core) {
    because("Brings injected mock mechanism. Caller should provide its own Mockito deps.")
  }

  compileOnly(Deps.AndroidX.Annotations)
  implementation(Deps.AndroidX.Test.Core)
  implementation(Deps.AndroidX.Test.Monitor)

  api(Deps.Javax.Inject.Inject)

  testImplementation(project(":publisher-sdk"))
  testImplementation(Deps.Kotlin.Stdlib)
  testImplementation(Deps.Mockito.Kotlin)
  testImplementation(Deps.AssertJ.AssertJ)

  androidTestImplementation(project(":publisher-sdk"))
  androidTestImplementation(Deps.AndroidX.Test.Runner)
  androidTestImplementation(Deps.AssertJ.AssertJ)
  androidTestImplementation(Deps.Mockito.Android)
  androidTestImplementation(Deps.Mockito.Kotlin)

  detektPlugins(Deps.Detekt.DetektFormatting)
}

addPublication("debug") {
  afterEvaluate {
    from(components["debug"])
    addSourcesJar("debug")
    addJavadocJar("debug")
  }

  artifactId = "criteo-publisher-sdk-test-utils"
  pom.description.set("Utilities for tests on the Criteo Publisher SDK")
}
