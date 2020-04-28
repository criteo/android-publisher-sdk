buildscript {
  addDefaultInputRepository()

  dependencies {
    classpath(Deps.Android.GradlePlugin)
    classpath(Deps.Kotlin.GradlePlugin)
    classpath(Deps.Vanniktech.DependencyGraphGenerator)
  }
}

allprojects {
  addDefaultInputRepository()
}
