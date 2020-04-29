plugins {
  `kotlin-dsl`
}

repositories {
  google()
  jcenter()
  gradlePluginPortal()
}

dependencies {
  implementation(gradleApi())
  implementation("com.android.tools.build:gradle:3.6.1")
  implementation("com.microsoft.azure:azure-storage:2.0.0")
  implementation("gradle.plugin.fr.pturpin.slackpublish:slack-publish:0.1.0")
}