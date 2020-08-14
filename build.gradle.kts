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

buildscript {
  addDefaultInputRepository()

  dependencies {
    classpath(Deps.Android.GradlePlugin)
    classpath(Deps.Kotlin.GradlePlugin)
    classpath(Deps.Kotlin.AllOpenPlugin)
  }
}

allprojects {
  addDefaultInputRepository()
}

plugins {
  id("org.sonarqube") version "3.0"
}

sonarqube {
  properties {
    property("sonar.projectKey", "com.criteo.publisher:criteo-publisher-sdk")
    property("sonar.organization", "criteo")
    property("sonar.host.url", "https://sonarcloud.io")

    // There is no dependency in the Gradle graph here. One should first generate coverage
    // reports before invoking the sonarqube task. In this way, the CI can run JVM and
    // Android tests in parallel, and when both are finished, CI would invoke this task.
    val jacocoReports = subprojects.flatMap {
      val reportTree = it.fileTree(it.buildDir)
      reportTree.include("reports/jacoco/**/*.xml") // Reports from JVM tests
      reportTree.include("reports/coverage/**/*.xml") // Reports from Android tests
      reportTree.files
    }
    property("sonar.coverage.jacoco.xmlReportPaths", jacocoReports)
  }
}
