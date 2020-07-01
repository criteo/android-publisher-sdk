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
  kotlin("android")
  id("com.vanniktech.android.javadoc") version "0.3.0"
  id("com.jfrog.bintray")
}

androidLibModule()

dependencies {
  compileOnly(project(":publisher-sdk"))

  implementation(Deps.JUnit.JUnit)
  implementation(Deps.Square.OkHttp.MockWebServer)

  compileOnly(Deps.Mockito.Core) {
    because("Brings injected mock mechanism. Caller should provide its own Mockito deps.")
  }

  compileOnly(Deps.AndroidX.Annotations)
  implementation(Deps.AndroidX.Test.Core)
  implementation(Deps.AndroidX.Test.Monitor)

  api(Deps.Javax.Inject.Inject)

  testImplementation(Deps.Kotlin.Stdlib)
  testImplementation(Deps.Mockito.Kotlin)
  testImplementation(Deps.AssertJ.AssertJ)
}

addPublication("debug") {
  afterEvaluate {
    from(components["debug"])
    addSourcesJar("debug")
    addJavadocJar("debug")
  }

  groupId = "com.criteo.publisher"
  artifactId = "criteo-publisher-sdk-test-utils"
  pom.description.set("Utilities for tests on the Criteo Publisher SDK")
}

addBintrayRepository()