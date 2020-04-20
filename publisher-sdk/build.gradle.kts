plugins {
    id("com.android.library")
    `maven-publish`
    kotlin("android")
}

androidLibModule() {
    // Network
    addBuildConfigField<String>("cdbUrl")
    addBuildConfigField<String>("remoteConfigUrl")
    addBuildConfigField<String>("eventUrl")
    addBuildConfigField<Int>("networkTimeoutInMillis")

    // Client side metrics
    addBuildConfigField<String>("csmQueueFilename")
    addBuildConfigField<String>("csmDirectoryName")
    addBuildConfigField<Int>("csmBatchSize")
    addBuildConfigField<Int>("maxSizeOfCsmMetricsFolder")
    addBuildConfigField<Int>("maxSizeOfCsmMetricSendingQueue")

    // Misc
    addBuildConfigField<Int>("profileId")
    addBuildConfigField<String>("pubSdkSharedPreferences")
    addBuildConfigField<Boolean>("debugLogging")
    addBuildConfigField<Boolean>("preconditionThrowsOnException")
}

addAzureRepository()

addPublication("release") {
    from(components["release"])
    groupId = "com.criteo.publisher"
    artifactId = "criteo-publisher-sdk"
}

dependencies {
    implementation(Deps.Android.Support.SupportCoreUtils)
    implementation(Deps.Square.Tape.Tape)

    compileOnly(Deps.Google.AdMob) {
        exclude(group = Deps.Android.Support.group)
    }

    implementation(Deps.AutoValue.Annotation)
    annotationProcessor(Deps.AutoValue.AutoValue)

    implementation(Deps.AutoValue.GsonRuntime)
    annotationProcessor(Deps.AutoValue.GsonExtension)

    // Optional @GsonTypeAdapterFactory support
    annotationProcessor(Deps.AutoValue.GsonFactory)

    testImplementation(project(":test-utils"))
    testImplementation(Deps.JUnit.JUnit)
    testImplementation(Deps.Mockito.Core)
    testImplementation(Deps.EqualsVerifier.EqualsVerifier)
    testImplementation(Deps.AssertJ.AssertJ)
    testImplementation(Deps.MockServer.Netty)
    testImplementation(Deps.MockServer.Client)
    testImplementation(Deps.Kotlin.Stdlib)
    testImplementation(Deps.Kotlin.JUnit)
    testImplementation(Deps.Mockito.Kotlin)

    androidTestImplementation(project(":test-utils"))
    androidTestImplementation(Deps.Android.Test.Runner)
    androidTestImplementation(Deps.Android.Test.Rules)
    androidTestImplementation(Deps.Mockito.Android)
    androidTestImplementation(Deps.Google.AdMob) {
        exclude(group = Deps.Android.Support.group)
    }

    androidTestImplementation(Deps.MoPub.Banner) {
        isTransitive = true
        exclude(group = Deps.Android.Support.group)
    }

    // Debug is needed because MoPub need some activities to be declare in the AndroidManifest.xml
    debugImplementation(Deps.MoPub.Interstitial) {
        isTransitive = true
        exclude(group = Deps.Android.Support.group)
    }
}
