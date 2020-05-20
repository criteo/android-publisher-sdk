plugins {
    id("com.android.library")
    `maven-publish`
    kotlin("android")
    id("com.vanniktech.dependency.graph.generator")
    id("fr.pturpin.slack-publish")
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

    // Advanced Native
    addBuildConfigField<Int>("adChoiceIconWidthInDp")
    addBuildConfigField<Int>("adChoiceIconHeightInDp")

    // Misc
    addBuildConfigField<Int>("profileId")
    addBuildConfigField<String>("pubSdkSharedPreferences")
    addBuildConfigField<Int>("minLogLevel")
    addBuildConfigField<Boolean>("preconditionThrowsOnException")
}

addAzureRepository()

// Declare release publication without sources
addPublication("release") {
    from(components["release"])
    groupId = "com.criteo.publisher"
    artifactId = "criteo-publisher-sdk"
}

// Declare both debug and staging publication with sources
for (variant in listOf("debug", "staging")) {
    addPublication(variant) {
        from(components[variant])
        groupId = "com.criteo.publisher"
        artifactId = "criteo-publisher-sdk-$variant"

        artifact(createSourcesJarTask(variant))
    }
}

addSlackDeploymentMessages()

dependencies {
    compileOnly(Deps.Android.Support.Annotations)
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

    implementation(Deps.Square.Picasso.Picasso) {
        exclude(group = Deps.Android.Support.group)
    }

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
    testImplementation(Deps.Android.Support.Annotations)
}
