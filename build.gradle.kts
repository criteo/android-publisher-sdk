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
    classpath(Deps.Jacoco.Core)
    classpath(Deps.Android.GradlePlugin)
    classpath(Deps.Kotlin.GradlePlugin)
    classpath(Deps.Kotlin.AllOpenPlugin)
  }
}

plugins {
  id("org.sonarqube") version "3.0"
  id("io.github.gradle-nexus.publish-plugin")
  id("com.github.ben-manes.versions") version "0.50.0"
}

allprojects {
  addDefaultInputRepository()
}

group = Deps.Criteo.PublisherSdk.group
addSonatypeOutputRepository()

sonarqube {
  properties {
    property("sonar.projectKey", "com.criteo.publisher:criteo-publisher-sdk")
    property("sonar.organization", "criteo")
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.projectVersion", sdkPublicationVersion())

    // There is no dependency in the Gradle graph here. One should first generate quality
    // reports before invoking the sonarqube task. In this way, the CI can run JVM and
    // Android tests in parallel, and when both are finished, CI would invoke this task.

    val jacocoReports =
        allSubProjectsReports("reports/jacoco/**/*.xml") + // Reports from JVM tests
            allSubProjectsReports("reports/coverage/**/*.xml") // Reports from Android tests
    property("sonar.coverage.jacoco.xmlReportPaths", jacocoReports)

    // We do not expect to cover the test app nor the dummy activities for Android tests
    property(
        "sonar.coverage.exclusions",
        listOf(
            "app/src/main/**/*",
            "publisher-sdk-tests/src/main/**/*",
            "test-utils/src/main/**/*" // FIXME EE-1370 handle coverage of test-utils from the SDK tests
        )
    )

    val junitReportFiles = allSubProjectsReports("**/TEST-*.xml") + // Normal tests
        allSubProjectsReports("test-results/gordon/*.xml") // Retried tests (Gordon runner)
    val junitReportDirs = junitReportFiles.mapNotNull { it.parentFile }.toSet()
    // FIXME EE-1335 Reactivate and fix declaration of JUnit reports to Sonar
    // property("sonar.junit.reportPaths", junitReportDirs)

    val lintReports = allSubProjectsReports("reports/lint-results.xml")
    property("sonar.androidLint.reportPaths", lintReports)

    val detektReports = allSubProjectsReports("reports/detekt/*.xml")
    property("sonar.kotlin.detekt.reportPaths", detektReports)
  }
}

fun Project.allSubProjectsReports(globPath: String): Set<File> {
  return subprojects.flatMap {
    val reportTree = it.fileTree(it.buildDir)
    reportTree.include(globPath)
    reportTree.files
  }.toSet()
}
