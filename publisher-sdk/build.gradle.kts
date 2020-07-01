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
    kotlin("android")
    id("com.vanniktech.dependency.graph.generator") version "0.5.0"
    id("com.vanniktech.android.javadoc") version "0.3.0"
    id("fr.pturpin.slack-publish")
    id("com.jfrog.bintray")
}

androidLibModule() {
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
    addBuildConfigField<Int>("profileId")
    addBuildConfigField<String>("pubSdkSharedPreferences")
    addBuildConfigField<Int>("minLogLevel")
    addBuildConfigField<Boolean>("preconditionThrowsOnException")
}

android.libraryVariants.all {
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

addAzureRepository()
addBintrayRepository()
addSlackDeploymentMessages()

dependencies {
    implementation(Deps.Kotlin.Stdlib)

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
}