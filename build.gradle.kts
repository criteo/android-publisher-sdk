buildscript {
  addDefaultInputRepository()

  dependencies {
    classpath(Deps.Android.GradlePlugin)
    classpath(Deps.Kotlin.GradlePlugin)
  }
}

allprojects {
  addDefaultInputRepository()
}
