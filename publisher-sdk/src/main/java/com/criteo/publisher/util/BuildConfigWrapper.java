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
  public String getRemoteConfigUrl() {
    return BuildConfig.remoteConfigUrl;
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

  /**
   * Profile ID used by the SDK, so CDB and the Supply chain can recognize that the request comes
   * from the PublisherSDK.
   */
  public int getProfileId() {
    return BuildConfig.profileId;
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
   * The relative path in application folder of the sending queue file
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
  public int getMinLogLevel() {
    return BuildConfig.minLogLevel;
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
}
