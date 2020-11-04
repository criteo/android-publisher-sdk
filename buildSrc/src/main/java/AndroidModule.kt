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
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import kotlin.reflect.KClass

class AndroidModule(private val project: Project) {

  private val configByName = mutableMapOf<String, ConfigObject?>()

  inline fun <reified T : Any> addBuildConfigField(name: String) {
    addBuildConfigField(name, T::class)
  }

  fun <T : Any> addBuildConfigField(name: String, klass: KClass<T>) {
    project.androidBase {
      buildTypes.all {
        getConfig(getName())?.let {
          when (klass) {
            String::class -> addStringField(name, it)
            Boolean::class -> addPrimitiveField(name, "boolean", it)
            Int::class -> addPrimitiveField(name, "int", it)
            else -> {
              throw UnsupportedOperationException()
            }
          }
        }
      }
    }
  }

  private fun BuildType.addStringField(name: String, config: ConfigObject) {
    buildConfigField("String", name, "\"${config[name]}\"")
  }

  private fun BuildType.addPrimitiveField(name: String, type: String, config: ConfigObject) {
    buildConfigField(type, name, "${config[name]}")
  }

  private fun getConfig(name: String): ConfigObject? {
    return configByName.computeIfAbsent(name) {
      val configFile = project.file("config.groovy")
      if (configFile.exists()) {
        ConfigSlurper(it).parse(configFile.toURL())
      } else {
        null
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

  configure(AndroidModule(this))
}

private fun Project.defaultAndroidModule() {
  androidBase {
    compileSdkVersion(29)
    buildToolsVersion("29.0.3")

    defaultConfig {
      minSdkVersion(16)
      targetSdkVersion(29)
      versionCode = 1
      versionName = sdkVersion()
      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        isTestCoverageEnabled = false
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
}