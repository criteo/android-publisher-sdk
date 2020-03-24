plugins {
    id("com.android.application")
    `maven-publish`
    kotlin("android")
}

androidAppModule("com.criteo.pubsdk_android")

android {
    flavorDimensions("mode")
    productFlavors {
        create("memoryLeaksHunt") {
            dimension = "mode"
            versionNameSuffix = "-memoryLeaksHunt"
        }
    }
}

// Export APK for all build types (release, staging, debug)
android.applicationVariants.all {
    val publicationName = name + "Apk"
    outputs.all {
        addPublication(publicationName) {
            groupId = "com.criteo.pubsdk_android"
            artifactId = "publisher-app"
            pom.packaging = "apk"

            artifact(outputFile) {
                classifier = buildType.name
                builtBy(assembleProvider)
            }
        }
    }
}

dependencies {
    implementation(project(":publisher-sdk"))

    implementation(Deps.Kotlin.Stdlib)
    implementation(Deps.Android.Support.AppCompatV7)
    implementation(Deps.Android.Support.Constraint.ConstraintLayout)
    implementation(Deps.Android.Support.Design)

    implementation(Deps.Google.AdMob) {
        exclude(group = Deps.Android.Support.group)
    }

    implementation(Deps.MoPub.Banner) {
        isTransitive = true
        exclude(group = Deps.Android.Support.group)
    }

    implementation(Deps.MoPub.Interstitial) {
        isTransitive = true
        exclude(group = Deps.Android.Support.group)
    }

    "memoryLeaksHuntImplementation"(Deps.Square.LeakCanary)
}
