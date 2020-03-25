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

  fun addStringBuildConfigField(name: String) {
    addBuildConfigField(name, String::class)
  }

  fun addBooleanBuildConfigField(name: String) {
    addBuildConfigField(name, Boolean::class)
  }

  fun <T : Any> addBuildConfigField(name: String, klass: KClass<T>) {
    project.androidBase {
      buildTypes.all {
        getConfig(getName())?.let {
          when (klass) {
            String::class -> addStringField(name, it)
            Boolean::class -> addBooleanField(name, it)
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

  private fun BuildType.addBooleanField(name: String, config: ConfigObject) {
    buildConfigField("boolean", name, "${config[name]}")
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

  if (hasPublishing()) {
    afterEvaluate {
      publishing {
        addNexusPreProdRepository()
        addNexusProdRepository()
      }
    }
  }
}