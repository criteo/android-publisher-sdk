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

tasks.register("printPublicationVersion") {
  group = "reporting"
  description = "Print the version that is used on publications"

  doLast {
    print(sdkPublicationVersion())
  }
}