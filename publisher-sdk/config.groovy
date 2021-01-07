pubSdkSharedPreferences = 'com.criteo.publisher.sdkSharedPreferences'

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

/**
 * Included default minimum level of logs to print
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
defaultMinLogLevel = 5 // Warning

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

/**
 * Remote logs configuration
 */

// The batch size of logs sent, at most, in each remote logs request.
// Given below estimation, a request can have a size of 1MB.
remoteLogBatchSize = 200

// The relative path in application folder of the sending queue file
remoteLogQueueFilename = 'criteo_remote_logs_queue'

// Maximum size (in bytes) of remote log elements stored in the sending queue.
// 250KB represents ~51 logs (with ~5000 bytes/log with big stacktrace) which already represent an extreme case.
maxSizeOfRemoteLogSendingQueue = 250 * 1024

environments {
    debug {
        eventUrl = 'https://an.url.that.does.not.exist'
        // eventUrl = 'https://gum.par.preprod.crto.in' // Uncomment to use preprod GUM

        // In tests, if DI rule is set, a CDB stub server is spawn and injected
        cdbUrl = 'https://an.url.that.does.not.exist'

        // Uncomment to use another CDB, you'll also need to deactivate the CDB stub in
        // MockedDependenciesRule
        cdbUrl = 'https://directbidder-test-app.par.preprod.crto.in' // preprod
        // cdbUrl = 'http://10.0.2.2:9991' // local

        defaultMinLogLevel = 2 // All
        preconditionThrowsOnException = true
    }

    staging {
        cdbUrl = 'https://bidder.criteo.com'
        eventUrl = 'https://gum.criteo.com'

        defaultMinLogLevel = 2 // All
    }

    release {
        cdbUrl = 'https://bidder.criteo.com'
        eventUrl = 'https://gum.criteo.com'
    }
}