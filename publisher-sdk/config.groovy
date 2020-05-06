pubSdkSharedPreferences = 'com.criteo.publisher.sdkSharedPreferences'
profileId = 235

/**
 * Included minimum level of logs to print
 * Values are from {@link android.util.Log}:
 * <ul>
 *     <li>2 = VERBOSE</li>
 *     <li>3 = DEBUG</li>
 *     <li>4 = INFO</li>
 *     <li>5 = WARNING</li>
 *     <li>6 = ERROR</li>
 *     <li>7 = ASSERT</li>
 * </ul>
 */
minLogLevel = 5 // Warning

/**
 * Indicate if exceptions that could be ignored should be thrown.
 * See PreconditionsUtil
 */
preconditionThrowsOnException = false

/**
 * Network configuration
 */

// FIXME EE-874 Review timeout duration
// Duration in milliseconds for the network layer to drop a call and consider it timeouted.
networkTimeoutInMillis = 60 * 1000

/**
 * Client-Side Metrics configuration
 */

// The relative path in application folder of the folder used to store metric files
csmDirectoryName = 'criteo_metrics'

// The relative path in application folder of the sending queue file
csmQueueFilename = 'criteo_metrics_queue'

// The batch size of metric files sent, at most, in each CSM requests.
// Bid requests are only bulked during the prefetch phase and CDB requests are split into chunks of,
// at most, 8 slots. This means that while CSM requests are emitted without issues, there should be,
// at most, 8 stored metrics. A batch size of 24 handles potential network issues and keeps the
// requests' size small (around 4KB given below estimation).
csmBatchSize = 24

// Maximum size (in bytes) of metric elements stored in the metrics folder.
// 48KB represents ~300 metrics (with ~164 bytes/metric) which already represent an extreme case.
maxSizeOfCsmMetricsFolder = 48 * 1024

// Maximum size (in bytes) of metric elements stored in the metric sending queue.
// 60KB represents ~360 metrics (with ~170 bytes/metric) which already represent an extreme case.
maxSizeOfCsmMetricSendingQueue = 60 * 1024

/**
 * Width and height in dp of the injected AdChoice icon for advanced native.
 */
adChoiceIconWidthInDp = 19
adChoiceIconHeightInDp = 15

environments {
    debug {
        cdbUrl = 'http://directbidder-test-app.par.preprod.crto.in'
        // cdbUrl = 'http://10.0.2.2:9991' // Uncomment to use local CDB instead of preprod
        remoteConfigUrl = 'https://pub-sdk-cfg.par.preprod.crto.in'
        eventUrl = 'https://gum.par.preprod.crto.in'

        minLogLevel = 2 // All
        preconditionThrowsOnException = true
    }

    staging {
        cdbUrl = 'https://bidder.criteo.com'
        remoteConfigUrl = 'https://pub-sdk-cfg.criteo.com'
        eventUrl = 'https://gum.criteo.com'

        minLogLevel = 2 // All
    }

    release {
        cdbUrl = 'https://bidder.criteo.com'
        remoteConfigUrl = 'https://pub-sdk-cfg.criteo.com'
        eventUrl = 'https://gum.criteo.com'
    }
}