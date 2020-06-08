plugins {
  id("com.android.library")
  kotlin("android")
}

androidLibModule()

android {
  defaultConfig {
    multiDexEnabled = true
  }

  packagingOptions {
    // Both AssertJ and ByteBuddy (via Mockito) brings this and the duplication yield an error
    exclude("META-INF/licenses/ASM")
  }
}

dependencies {
  implementation(project(":publisher-sdk"))
  implementation(project(":test-utils"))
  implementation(Deps.Kotlin.Stdlib)
  implementation(Deps.AndroidX.MultiDex)
  implementation(Deps.AndroidX.Annotations)
  implementation(Deps.AndroidX.RecyclerView)

  androidTestImplementation(Deps.AndroidX.SupportCoreUtils)
  androidTestImplementation(Deps.AndroidX.Test.Runner)
  androidTestImplementation(Deps.AndroidX.Test.Rules)
  androidTestImplementation(Deps.Mockito.Android)
  androidTestImplementation(Deps.Mockito.Kotlin)
  androidTestImplementation(Deps.AssertJ.AssertJ)
  androidTestImplementation(Deps.Square.Tape.Tape)
  androidTestImplementation(Deps.Google.AdMob)

  androidTestImplementation(Deps.MoPub.Banner) {
    isTransitive = true
  }

  // Debug is needed because MoPub need some activities to be declare in the AndroidManifest.xml
  debugImplementation(Deps.MoPub.Interstitial) {
    isTransitive = true
  }
}
