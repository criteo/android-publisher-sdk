plugins {
  id("com.android.library")
  `maven-publish`
  kotlin("android")
}

androidLibModule()

dependencies {
  compileOnly(project(":publisher-sdk"))

  implementation(Deps.JUnit.JUnit)
  implementation(Deps.Mockito.Core)

  compileOnly(Deps.Android.Support.Annotations)
  implementation(Deps.Android.Support.RecyclerViewV7)
  implementation(Deps.Android.Test.Monitor) {
    exclude(group = Deps.Android.Support.group)
  }

  api(Deps.Javax.Inject.Inject)

  testImplementation(Deps.Kotlin.Stdlib)
  testImplementation(Deps.Mockito.Kotlin)
  testImplementation(Deps.AssertJ.AssertJ)
}

addPublication("debug") {
  from(components["debug"])
  groupId = "com.criteo.publisher"
  artifactId = "criteo-publisher-sdk-test-utils"

  artifact(createSourcesJarTask("debug"))
}
