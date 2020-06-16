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

    defaultConfig {
        multiDexEnabled = true
    }
}

// Export APK for all build types (release, staging, debug)
addPublication("Apk") {
    groupId = "com.criteo.pubsdk_android"
    artifactId = "publisher-app"
    pom.packaging = "apk"

    android.applicationVariants.all {
        outputs.all {
            artifact(outputFile) {
                classifier = buildType.name
                builtBy(assembleProvider)
            }
        }
    }
}

dependencies {
    implementation(project(":publisher-sdk"))

    implementation(Deps.Square.Picasso.Picasso)

    // FIXME EE-1097 Adapters need SDK vX, but test app needs adapters vX, so when bumping version,
    //  one dependency should be cut off.
//    implementation(Deps.Criteo.Mediation.MoPub("${sdkVersion()}+")) {
//        exclude(group = Deps.Criteo.PublisherSdk.group)
//    }
//
//    implementation(Deps.Criteo.Mediation.AdMob("${sdkVersion()}+")) {
//        exclude(group = Deps.Criteo.PublisherSdk.group)
//    }

    implementation(Deps.Kotlin.Stdlib)
    implementation(Deps.AndroidX.MultiDex)
    implementation(Deps.AndroidX.AppCompat)
    implementation(Deps.AndroidX.Constraint.ConstraintLayout)
    implementation(Deps.AndroidX.MaterialComponents)

    implementation(Deps.Google.AdMob)

    implementation(Deps.MoPub.Banner) {
        isTransitive = true
    }

    implementation(Deps.MoPub.Interstitial) {
        isTransitive = true
    }

    implementation(Deps.MoPub.Native) {
        isTransitive = true
    }

    "memoryLeaksHuntImplementation"(Deps.Square.LeakCanary.LeakCanary)
}
