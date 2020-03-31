pubSdkSharedPreferences = 'com.criteo.publisher.sdkSharedPreferences'
debugLogging = false
profileId = 235

/**
 * Client-Side Metrics configuration
 */

// The relative path in application folder of the folder used to store metric files
csmDirectory = 'criteo_metrics'

// FIXME EE-991 Document how to determine this value + determine it
// The batch size of metric files sent, at most, in each CSM requests.
csmBatchSize = 5

// Maximum size (in bytes) of metric elements stored in the metrics folder.
// 48KB represents ~300 metrics (with ~164 bytes/metric) which already represent an extreme case.
maxSizeOfCsmMetricsFolder = 48 * 1024

environments {
    debug {
        cdbUrl = 'http://directbidder-test-app.par.preprod.crto.in'
        remoteConfigUrl = 'https://pub-sdk-cfg.par.preprod.crto.in'
        eventUrl = 'https://gum.par.preprod.crto.in'

        debugLogging = true
    }

    staging {
        cdbUrl = 'https://bidder.criteo.com'
        remoteConfigUrl = 'https://pub-sdk-cfg.criteo.com'
        eventUrl = 'https://gum.criteo.com'

        debugLogging = true
    }

    release {
        cdbUrl = 'https://bidder.criteo.com'
        remoteConfigUrl = 'https://pub-sdk-cfg.criteo.com'
        eventUrl = 'https://gum.criteo.com'
    }
}