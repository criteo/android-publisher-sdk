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
    id("com.android.library")
    `maven-publish`
    jacoco
    kotlin("android")
    id("kotlin-allopen")
    id("com.vanniktech.dependency.graph.generator") version "0.5.0"
    id("com.vanniktech.android.javadoc") version "0.3.0"
    id("fr.pturpin.slack-publish")
    id("com.jfrog.bintray")
    id("io.gitlab.arturbosch.detekt")
    id("com.banno.gordon")
}

gordon.retryQuota.set(5)

allOpen {
    annotation("com.criteo.publisher.annotation.OpenForTesting")
}

androidLibModule {
    // Network
    addBuildConfigField<String>("cdbUrl")
    addBuildConfigField<String>("eventUrl")
    addBuildConfigField<Int>("networkTimeoutInMillis")

    // Client side metrics
    addBuildConfigField<String>("csmQueueFilename")
    addBuildConfigField<String>("csmDirectoryName")
    addBuildConfigField<Int>("csmBatchSize")
    addBuildConfigField<Int>("maxSizeOfCsmMetricsFolder")
    addBuildConfigField<Int>("maxSizeOfCsmMetricSendingQueue")

    // Advanced Native
    addBuildConfigField<Int>("adChoiceIconWidthInDp")
    addBuildConfigField<Int>("adChoiceIconHeightInDp")

    // Misc
    addBuildConfigField<String>("pubSdkSharedPreferences")
    addBuildConfigField<Int>("defaultMinLogLevel")
    addBuildConfigField<Boolean>("preconditionThrowsOnException")
}

android {
    defaultConfig {
        multiDexEnabled = true
    }

    packagingOptions {
        // Both AssertJ and ByteBuddy (via Mockito) brings this and the duplication yield an error
        exclude("META-INF/licenses/ASM")
    }

    libraryVariants.all {
        val variantName = name
        addPublication(variantName) {
            afterEvaluate {
                from(components[variantName])
                addSourcesJar(variantName)
                addJavadocJar(variantName)
            }

            groupId = "com.criteo.publisher"

            artifactId = if (variantName == "release" && isSnapshot()) {
                "criteo-publisher-sdk-development"
            } else if (variantName == "release") {
                "criteo-publisher-sdk"
            } else {
                "criteo-publisher-sdk-$variantName"
            }

            pom.description.set(Publications.sdkDescription)
        }
    }
}

addBintrayRepository()
addSlackDeploymentMessages()

configurations.configureEach {
    resolutionStrategy {
        // Picasso use a old version of OkHttp, but MockWebServer need a recent one
        force(Deps.Square.OkHttp.OkHttp)
    }
}

dependencies {
    implementation(Deps.Kotlin.Stdlib)
    implementation(Deps.AndroidX.MultiDex)

    compileOnly(Deps.AndroidX.Annotations)
    implementation(Deps.Square.Tape.Tape)

    compileOnly(Deps.Google.AdMob)

    implementation(Deps.AutoValue.Annotation)
    annotationProcessor(Deps.AutoValue.AutoValue)

    implementation(Deps.AutoValue.GsonRuntime)
    annotationProcessor(Deps.AutoValue.GsonExtension)

    // Optional @GsonTypeAdapterFactory support
    annotationProcessor(Deps.AutoValue.GsonFactory)

    implementation(Deps.Square.Picasso.Picasso)

    testImplementation(project(":test-utils"))
    testImplementation(Deps.JUnit.JUnit)
    testImplementation(Deps.Mockito.Core)
    testImplementation(Deps.EqualsVerifier.EqualsVerifier)
    testImplementation(Deps.AssertJ.AssertJ)
    testImplementation(Deps.Json.Json)
    testImplementation(Deps.Square.OkHttp.MockWebServer)
    testImplementation(Deps.Kotlin.JUnit)
    testImplementation(Deps.Mockito.Kotlin)
    testImplementation(Deps.AndroidX.Annotations)

    androidTestImplementation(project(":test-utils"))
    androidTestImplementation(project(":publisher-sdk-tests"))
    androidTestImplementation(Deps.AndroidX.SupportCoreUtils)
    androidTestImplementation(Deps.AndroidX.Test.Runner)
    androidTestImplementation(Deps.AndroidX.Test.Rules)
    androidTestImplementation(Deps.Mockito.Android)
    androidTestImplementation(Deps.Mockito.Kotlin)
    androidTestImplementation(Deps.AssertJ.AssertJ)
    androidTestImplementation(Deps.Square.Tape.Tape)
    androidTestImplementation(Deps.Square.OkHttp.MockWebServer)
    androidTestImplementation(Deps.Google.AdMob)
    androidTestImplementation(Deps.MoPub.Banner) { isTransitive = true }
    androidTestImplementation(Deps.MoPub.Interstitial) { isTransitive = true }

    detektPlugins(Deps.Detekt.DetektFormatting)
}
