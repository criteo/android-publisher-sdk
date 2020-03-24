plugins {
    id("com.android.library")
    `maven-publish`
    kotlin("android")
}

androidLibModule() {
    addStringBuildConfigField("cdbUrl")
    addStringBuildConfigField("remoteConfigUrl")
    addStringBuildConfigField("eventUrl")
    addStringBuildConfigField("pubSdkSharedPreferences")
    addStringBuildConfigField("csmDirectory")
    addBooleanBuildConfigField("debugLogging")
}

addAzureRepository()

addPublication("release") {
    from(components["release"])
    groupId = "com.criteo.publisher"
    artifactId = "criteo-publisher-sdk"
}

dependencies {
    implementation(Deps.Android.Support.SupportCoreUtils)

    compileOnly(Deps.Google.AdMob) {
        exclude(group = Deps.Android.Support.group)
    }

    implementation(Deps.AutoValue.Annotation)
    annotationProcessor(Deps.AutoValue.AutoValue)

    implementation(Deps.AutoValue.GsonRuntime)
    annotationProcessor(Deps.AutoValue.GsonExtension)

    // Optional @GsonTypeAdapterFactory support
    annotationProcessor(Deps.AutoValue.GsonFactory)

    testImplementation(Deps.JUnit.JUnit)
    testImplementation(Deps.Mockito.Core)
    testImplementation(Deps.EqualsVerifier.EqualsVerifier)
    testImplementation(Deps.AssertJ.AssertJ)
    testImplementation(Deps.MockServer.Netty)
    testImplementation(Deps.MockServer.Client)
    testImplementation(Deps.Kotlin.Stdlib)
    testImplementation(Deps.Kotlin.JUnit)
    testImplementation(Deps.Mockito.Kotlin)

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
