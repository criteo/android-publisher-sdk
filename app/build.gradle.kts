/*
apply plugin: 'com.android.application'
apply plugin: 'maven-publish'
apply plugin: 'kotlin-android'

def groovyHelper = new GroovyHelper()

def support_version = "28.0.0"

groovyHelper.androidAppModule(project, "com.criteo.pubsdk_android")

android {
    flavorDimensions "mode"
    productFlavors {
        memoryLeaksHunt {
            dimension "mode"
            versionNameSuffix "-memoryLeaksHunt"
        }
    }
}

// Export APK for all build types (release, staging, debug)
android.applicationVariants.all { variant ->
    outputs.all {
        groovyHelper.addPublication(project, variant.name + "Apk") {
            it.groupId = "com.criteo.pubsdk_android"
            it.artifactId = "publisher-app"
            it.pom.packaging = "apk"

            it.artifact(outputFile) {
                classifier = variant.buildType.name
                builtBy(assembleProvider)
            }
        }
    }
}

dependencies {
    implementation project(':publisher-sdk')
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation "com.android.support:appcompat-v7:$support_version"
    implementation "com.android.support.constraint:constraint-layout:1.1.3"
    implementation "com.android.support:design:$support_version"

    // For Google AdMob
    implementation("com.google.android.gms:play-services-ads:15.0.1") {
        exclude group: "com.android.support"
    }

    // For MoPub banners
    implementation('com.mopub:mopub-sdk-banner:5.6.0@aar') {
        transitive = true
        exclude group: "com.android.support"
    }

    // For MoPub interstitials
    implementation('com.mopub:mopub-sdk-interstitial:5.6.0@aar') {
        transitive = true
        exclude group: "com.android.support"
    }

    memoryLeaksHuntImplementation('com.squareup.leakcanary:leakcanary-android:2.1')
}
*/