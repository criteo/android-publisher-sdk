object Deps {

  object Android {

    private const val agpVersion = "3.6.1"
    const val GradlePlugin = "com.android.tools.build:gradle:$agpVersion"
  }

  object AndroidX {
    const val AppCompat = "androidx.appcompat:appcompat:1.1.0"
    const val Annotations = "androidx.annotation:annotation:1.0.0"
    const val MaterialComponents = "com.google.android.material:material:1.0.0"
    const val MultiDex = "androidx.multidex:multidex:2.0.0"
    const val RecyclerView = "androidx.recyclerview:recyclerview:1.1.0"
    const val SupportCoreUtils = "androidx.legacy:legacy-support-core-utils:1.0.0"

    object Constraint {
      const val ConstraintLayout = "androidx.constraintlayout:constraintlayout:2.0.0-beta6"
    }

    object Test {
      private const val version = "1.2.0"
      const val Monitor = "androidx.test:monitor:$version"
      const val Runner = "androidx.test:runner:$version"
      const val Rules = "androidx.test:rules:$version"
    }
  }

  object AssertJ {
    private const val version = "3.11.1"

    const val AssertJ = "org.assertj:assertj-core:$version"
  }

  object AutoValue {
    private const val googleVersion = "1.6.6"
    private const val gsonVersion = "1.3.0"

    const val Annotation = "com.google.auto.value:auto-value-annotations:$googleVersion"
    const val AutoValue = "com.google.auto.value:auto-value:$googleVersion"
    const val GsonRuntime = "com.ryanharter.auto.value:auto-value-gson-runtime:$gsonVersion"
    const val GsonExtension = "com.ryanharter.auto.value:auto-value-gson-extension:$gsonVersion"
    const val GsonFactory = "com.ryanharter.auto.value:auto-value-gson-factory:$gsonVersion"
  }

  object Criteo {
    object PublisherSdk {
      const val group = "com.criteo.publisher"
    }

    object Mediation {
      fun MoPub(version: String) = "com.criteo.mediation.mopub:criteo-adapter:$version"
      fun AdMob(version: String) = "com.criteo.mediation.google:criteo-adapter:$version"
    }
  }

  object EqualsVerifier {
    private const val version = "3.1.10"

    const val EqualsVerifier = "nl.jqno.equalsverifier:equalsverifier:$version"
  }

  object Google {
    private const val version = "15.0.1"

    const val AdMob = "com.google.android.gms:play-services-ads:$version"
  }

  object Javax {
    object Inject {
      private const val version = "1"

      const val Inject = "javax.inject:javax.inject:$version"
    }
  }

  object JUnit {
    private const val version = "4.12"

    const val JUnit = "junit:junit:$version"
  }

  object Kotlin {
    private const val version = "1.3.70"

    const val GradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
    const val JUnit = "org.jetbrains.kotlin:kotlin-test-junit:$version"
    const val Stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
  }

  object Mockito {
    private const val version = "3.3.0"

    const val Android = "org.mockito:mockito-android:$version"
    const val Core = "org.mockito:mockito-core:$version"
    const val Kotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0"
  }

  object MockServer {
    private const val version = "5.8.1"

    const val Netty = "org.mock-server:mockserver-netty:$version"
    const val Client = "org.mock-server:mockserver-client-java:$version"
  }

  object MoPub {
    private const val version = "5.10.0"

    const val Banner = "com.mopub:mopub-sdk-banner:$version@aar"
    const val Interstitial = "com.mopub:mopub-sdk-interstitial:$version@aar"
  }

  object Square {
    object LeakCanary {
      private const val version = "2.3"

      const val LeakCanary = "com.squareup.leakcanary:leakcanary-android:$version"
    }

    object Picasso {
      private const val version = "2.71828"

      const val Picasso = "com.squareup.picasso:picasso:$version"
    }

    object Tape {
      private const val version = "1.2.3"

      const val Tape = "com.squareup:tape:$version"
    }
  }

  object Vanniktech {
    private const val version = "0.5.0"
    const val DependencyGraphGenerator = "com.vanniktech:gradle-dependency-graph-generator-plugin:$version"
  }
}
