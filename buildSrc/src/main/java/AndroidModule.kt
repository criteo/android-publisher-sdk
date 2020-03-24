import com.android.build.gradle.internal.dsl.BuildType
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting

fun Project.androidAppModule(applicationId: String) {
  defaultAndroidModule()

  androidApp {
    defaultConfig.applicationId = applicationId
  }
}

fun Project.androidLibModule() {
  defaultAndroidModule()
}

private fun Project.defaultAndroidModule() {
  androidBase {
    compileSdkVersion(28)

    defaultConfig {
      minSdkVersion(16)
      targetSdkVersion(27)
      versionCode = 1
      versionName = sdkVersion()
      testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
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

      file("lint.xml")
          .takeIf { it.exists() }
          ?.let { lintConfig = it }
    }

    testOptions {
      unitTests.isReturnDefaultValues = true
    }
  }

  afterEvaluate {
    publishing {
      addNexusPreProdRepository()
      addNexusProdRepository()
    }
  }
}