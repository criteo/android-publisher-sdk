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

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.*
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.File

/**
 * Add new task generating XML and HTML Jacoco reports for given project.
 *
 * This is only done for coverage data gathered via JVM tests (unit tests). The Android Jacoco
 * tasks are completely independent from Gradle ones and generate themselves XML and HTML reports.
 *
 * The new task is called `codeCoverageReport` and is automatically triggered when `check` is run.
 *
 * Note that only reports of debug build type are generated.
 */
fun Project.generateCoverageReportForJvmTests() {
  val variant = "debug"

  // This wait for the setup of the plugin in subproject if it is applied.
  // So subprojects should opt-in to get their coverage.
  plugins.withType<JacocoPlugin>().configureEach {
    val reportTask = tasks.register<JacocoReport>("jacocoReport${variant.capitalize()}UnitTest") {
      group = "verification"
      description = "Generate coverage report of JVM tests on $variant build"

      sourceDirectories.from(sourceDirs(variant))
      classDirectories.from(classFilesForCoverage(variant))
      executionData.from(jacocoExecutionFiles(variant))

      reports {
        xml.isEnabled = true
        html.isEnabled = true
      }
    }

    tasks.named("check") {
      dependsOn(reportTask)
    }
  }
}

private fun Project.sourceDirs(variant: String): Set<File> {
  val android = extensions.getByType(com.android.build.gradle.BaseExtension::class)
  return android.sourceSets["main"].java.srcDirs union android.sourceSets[variant].java.srcDirs
}

private fun Project.classFilesForCoverage(variant: String): FileCollection {
  val excludedGeneratedClasses = listOf(
      "**/R.class",
      "**/R$*.class",
      "**/BuildConfig.class"
  )

  return fileTree(buildDir).apply {
    include("intermediates/javac/$variant/classes/**/*.class") // Java classes
    include("tmp/kotlin-classes/$variant/**/*.class") // Kotlin classes
    exclude(excludedGeneratedClasses)
  }
}

private fun Project.jacocoExecutionFiles(variant: String): FileCollection {
  val jacocoTasks = tasks.matching {
    it.extensions.findByType<JacocoTaskExtension>() != null
  }

  return fileTree(buildDir).apply {
    include("**/jacoco/*${variant.capitalize()}*.exec")
    builtBy(jacocoTasks)
  }
}