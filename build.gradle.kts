buildscript {
  addDefaultInputRepository()

  dependencies {
    classpath("com.android.tools.build:gradle:3.6.1")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
  }
}

allprojects {
  addDefaultInputRepository()
}
