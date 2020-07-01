import Deps.Criteo.Mediation.adMobAdapter
import Deps.Criteo.Mediation.moPubAdapter

/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
    groupId = "com.criteo.publisher"
    artifactId = "criteo-publisher-sdk-test-app"

    pom {
        description.set("Test Application for the Criteo Publisher SDK")
        packaging = "apk"
    }

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

    implementation(moPubAdapter("(,${sdkVersion()}.99)")) {
        exclude(group = Deps.Criteo.PublisherSdk.group)
        isChanging = true
        because("""
            Select the biggest available version up to the current SDK version.
            This allows bumping the SDK version without having trouble even if this adapter is not
            yet upgraded.
            The .99 is needed because Gradle range does not support + syntax in the range syntax
            """.trimIndent())
    }

    implementation(adMobAdapter("(,${sdkVersion()}.99)")) {
        exclude(group = Deps.Criteo.PublisherSdk.group)
        isChanging = true
        because("""
            Select the biggest available version up to the current SDK version.
            This allows bumping the SDK version without having trouble even if this adapter is not
            yet upgraded.
            The .99 is needed because Gradle range does not support + syntax in the range syntax
            """.trimIndent())
    }

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
