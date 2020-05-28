plugins {
  id("com.android.library")
  `maven-publish`
  kotlin("android")
}

androidLibModule()

dependencies {
  compileOnly(project(":publisher-sdk"))

  implementation(Deps.JUnit.JUnit)
  compileOnly(Deps.Mockito.Core) {
    because("Brings injected mock mechanism. Caller should provide its own Mockito deps.")
  }

  compileOnly(Deps.AndroidX.Annotations)
  implementation(Deps.AndroidX.Test.Monitor)

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