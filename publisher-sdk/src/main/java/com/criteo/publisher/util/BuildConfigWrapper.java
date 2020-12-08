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

package com.criteo.publisher.util;

import androidx.annotation.NonNull;
import com.criteo.publisher.BuildConfig;

/**
 * Simple wrapper around {@link BuildConfig} constants to ease testing
 */
public class BuildConfigWrapper {

  @NonNull
  public String getSdkVersion() {
    return BuildConfig.VERSION_NAME;
  }

  @NonNull
  public String getCdbUrl() {
    return BuildConfig.cdbUrl;
  }

  @NonNull
  public String getEventUrl() {
    return BuildConfig.eventUrl;
  }

  /**
   * Indicate if exceptions that could be ignored should be thrown.
   *
   * @see PreconditionsUtil
   */
  public boolean preconditionThrowsOnException() {
    return BuildConfig.preconditionThrowsOnException;
  }

  public int getCsmBatchSize() {
    return BuildConfig.csmBatchSize;
  }

  /**
   * Maximum size (in bytes) of metric elements stored in the metrics folder.
   */
  public int getMaxSizeOfCsmMetricsFolder() {
    return BuildConfig.maxSizeOfCsmMetricsFolder;
  }

  /**
   * Maximum size (in bytes) of metric elements stored in the metric sending queue.
   */
  public int getMaxSizeOfCsmMetricSendingQueue() {
    return BuildConfig.maxSizeOfCsmMetricSendingQueue;
  }

  /**
   * The relative path in application folder of the sending queue file for CSM
   */
  @NonNull
  public String getCsmQueueFilename() {
    return BuildConfig.csmQueueFilename;
  }

  /**
   * The relative path in application folder of the folder used to store metric files
   */
  @NonNull
  public String getCsmDirectoryName() {
    return BuildConfig.csmDirectoryName;
  }

  /**
   * Duration in milliseconds for the network layer to drop a call and consider it timeouted.
   */
  public int getNetworkTimeoutInMillis() {
    return BuildConfig.networkTimeoutInMillis;
  }

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
  public int getDefaultMinLogLevel() {
    return BuildConfig.defaultMinLogLevel;
  }

  /**
   * Width in dp of the AdChoice icon for advanced native.
   */
  public int getAdChoiceIconWidthInDp() {
    return BuildConfig.adChoiceIconWidthInDp;
  }

  /**
   * Height in dp of the AdChoice icon for advanced native.
   */
  public int getAdChoiceIconHeightInDp() {
    return BuildConfig.adChoiceIconHeightInDp;
  }

  public int getRemoteLogBatchSize() {
    return BuildConfig.remoteLogBatchSize;
  }

  /**
   * Maximum size (in bytes) of metric elements stored in the remote log sending queue.
   */
  public int getMaxSizeOfRemoteLogSendingQueue() {
    return BuildConfig.maxSizeOfRemoteLogSendingQueue;
  }

  /**
   * The relative path in application folder of the sending queue file for remote logs
   */
  @NonNull
  public String getRemoteLogQueueFilename() {
    return BuildConfig.remoteLogQueueFilename;
  }
}
