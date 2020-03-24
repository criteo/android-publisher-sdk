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

val support_version = "28.0.0"
val kotlin_version = "1.3.61"

dependencies {
    implementation(project(":publisher-sdk"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("com.android.support:appcompat-v7:$support_version")
    implementation("com.android.support.constraint:constraint-layout:1.1.3")
    implementation("com.android.support:design:$support_version")

    // For Google AdMob
    implementation("com.google.android.gms:play-services-ads:15.0.1") {
        exclude(group = "com.android.support")
    }

    // For MoPub banners
    implementation("com.mopub:mopub-sdk-banner:5.6.0@aar") {
        isTransitive = true
        exclude(group = "com.android.support")
    }

    // For MoPub interstitials
    implementation("com.mopub:mopub-sdk-interstitial:5.6.0@aar") {
        isTransitive = true
        exclude(group = "com.android.support")
    }

    "memoryLeaksHuntImplementation"("com.squareup.leakcanary:leakcanary-android:2.1")
}
