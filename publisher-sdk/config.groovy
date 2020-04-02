pubSdkSharedPreferences = 'com.criteo.publisher.sdkSharedPreferences'
debugLogging = false
profileId = 235

/**
 * Client-Side Metrics configuration
 */

// The relative path in application folder of the folder used to store metric files
csmDirectoryName = 'criteo_metrics'

// The relative path in application folder of the sending queue file
csmQueueFilename = 'criteo_metrics_queue'

// FIXME EE-991 Document how to determine this value + determine it
// The batch size of metric files sent, at most, in each CSM requests.
csmBatchSize = 5

// Maximum size (in bytes) of metric elements stored in the metrics folder.
// 48KB represents ~300 metrics (with ~164 bytes/metric) which already represent an extreme case.
maxSizeOfCsmMetricsFolder = 48 * 1024

// Maximum size (in bytes) of metric elements stored in the metric sending queue.
// 60KB represents ~360 metrics (with ~170 bytes/metric) which already represent an extreme case.
maxSizeOfCsmMetricSendingQueue = 60 * 1024

environments {
    debug {
        cdbUrl = 'http://directbidder-test-app.par.preprod.crto.in'
        // cdbUrl = 'http://10.0.2.2:9991' // Uncomment to use local CDB instead of preprod
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