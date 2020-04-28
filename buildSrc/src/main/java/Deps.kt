object Deps {

  object Android {

    private const val agpVersion = "3.6.1"
    const val GradlePlugin = "com.android.tools.build:gradle:$agpVersion"

    object Support {
      private const val version = "28.0.0"

      const val group = "com.android.support"
      const val AppCompatV7 = "com.android.support:appcompat-v7:$version"
      const val Annotations = "com.android.support:support-annotations:$version"
      const val Design = "com.android.support:design:$version"
      const val SupportCoreUtils = "com.android.support:support-core-utils:$version"

      object Constraint {
        private const val version = "1.1.3"

        const val ConstraintLayout = "com.android.support.constraint:constraint-layout:$version"
      }
    }

    object Test {
      private const val version = "1.0.2"

      const val Monitor = "com.android.support.test:monitor:$version"
      const val Runner = "com.android.support.test:runner:$version"
      const val Rules = "com.android.support.test:rules:$version"
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
    private const val version = "1.3.61"

    const val GradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
    const val JUnit = "org.jetbrains.kotlin:kotlin-test-junit:$version"
    const val Stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
  }

  object Mockito {
    const val Android = "org.mockito:mockito-android:3.3.0"
    const val Core = "org.mockito:mockito-core:2.7.0"
    const val Kotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0"
  }

  object MockServer {
    private const val version = "5.8.1"

    const val Netty = "org.mock-server:mockserver-netty:$version"
    const val Client = "org.mock-server:mockserver-client-java:$version"
  }

  object MoPub {
    private const val version = "5.6.0"

    const val Banner = "com.mopub:mopub-sdk-banner:$version@aar"
    const val Interstitial = "com.mopub:mopub-sdk-interstitial:$version@aar"
  }

  object Square {
    object LeakCanary {
      private const val version = "2.1"

      const val LeakCanary = "com.squareup.leakcanary:leakcanary-android:$version"
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
