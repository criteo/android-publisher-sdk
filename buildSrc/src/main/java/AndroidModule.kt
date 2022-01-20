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

import com.android.build.gradle.internal.dsl.BuildType
import groovy.util.ConfigObject
import groovy.util.ConfigSlurper
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*

class AndroidModule(private val project: Project) {

  private val configByName = mutableMapOf<String, ConfigObject>()

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> addBuildConfigField(name: String) {
    addBuildConfigField(name) {
      getConfig(getName())[name] as T
    }
  }

  fun <T : Any> addBuildConfigField(name: String, value: T) {
    addBuildConfigField(name) { value }
  }

  fun <T : Any> addBuildConfigField(name: String, getter: BuildType.() -> T) {
    project.androidBase {
      buildTypes.all {
        when (val value = getter(this)) {
          is String -> addStringField(name, value)
          is Boolean -> addPrimitiveField(name, "boolean", value)
          is Int -> addPrimitiveField(name, "int", value)
          else -> {
            throw UnsupportedOperationException()
          }
        }
      }
    }
  }

  private fun BuildType.addStringField(name: String, value: String) {
    buildConfigField("String", name, "\"$value\"")
  }

  private fun BuildType.addPrimitiveField(name: String, type: String, value: Any) {
    buildConfigField(type, name, "$value")
  }

  private fun getConfig(name: String): ConfigObject {
    return configByName.computeIfAbsent(name) {
      val configFile = project.file("config.groovy")
      if (configFile.exists()) {
        ConfigSlurper(it).parse(configFile.toURI().toURL())
      } else {
        throw UnsupportedOperationException("Missing config.groovy file")
      }
    }
  }
}

fun Project.androidAppModule(applicationId: String, configure: AndroidModule.() -> Unit = {}) {
  defaultAndroidModule()

  androidApp {
    defaultConfig.applicationId = applicationId
  }

  configure(AndroidModule(this))
}

fun Project.androidLibModule(configure: AndroidModule.() -> Unit = {}) {
  defaultAndroidModule()

  configure(AndroidModule(this).apply {
    // Version stopped to be injected for library
    // https://developer.android.com/studio/releases/gradle-plugin?buildsystem=ndk-build#version_properties_removed_from_buildconfig_class_in_library_projects
    addBuildConfigField("VERSION_NAME", sdkVersion())
  })
}

private fun Project.defaultAndroidModule() {
  androidBase {
    compileSdkVersion(30)

    defaultConfig {
      minSdkVersion(16)
      targetSdkVersion(30)
      versionCode = 1
      versionName = sdkVersion()
      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      testInstrumentationRunnerArgument("disableAnalytics", "true")
    }

    fun BuildType.addProguardIfExists() {
      val filename = "proguard-rules.pro"
      if (file(filename).exists()) {
        proguardFiles(getDefaultProguardFile("proguard-android.txt"), filename)
      }
    }

    fun BuildType.addConsumerProguardIfExists() {
      val filename = "consumer-proguard.txt"
      if (file(filename).exists()) {
        consumerProguardFile(filename)
      }
    }

    buildTypes {
      getByName("release") {
        isMinifyEnabled = true
        isDebuggable = true
        addProguardIfExists()
        addConsumerProguardIfExists()
      }
      val debug by getting {
        isMinifyEnabled = false
        isDebuggable = true
        isTestCoverageEnabled = true
        addProguardIfExists()
        addConsumerProguardIfExists()
      }
      create("staging") {
        initWith(debug)
      }
    }

    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_1_8
      targetCompatibility = JavaVersion.VERSION_1_8
    }

    lintOptions {
      isAbortOnError = true

      file("lint.xml").takeIf { it.exists() }
          ?.let { lintConfig = it }
    }

    testOptions {
      unitTests.isReturnDefaultValues = true
    }

    jacoco {
      version = Deps.Jacoco.version
    }

    packagingOptions {
      // Both AssertJ and ByteBuddy (via Mockito) bring this and the duplication yields an error
      exclude("META-INF/licenses/ASM")
    }
  }

  if (hasPublishing()) {
    publishing {
      addDevRepository()
    }
  }

  detekt?.apply {
    toolVersion = Deps.Detekt.version
    config = files(rootDir.resolve(".detekt/config.yml"))
  }

  generateCoverageReportForJvmTests()

  jacoco?.apply {
    toolVersion = Deps.Jacoco.version
  }

  afterEvaluate {
    tasks.withType<JavaCompile> {
      options.compilerArgs.add("-Xlint:deprecation")
    }
  }
}