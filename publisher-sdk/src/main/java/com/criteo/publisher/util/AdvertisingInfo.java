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

import android.content.Context;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import com.criteo.publisher.SafeRunnable;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

public class AdvertisingInfo {

  @NonNull
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @NonNull
  private final SafeAdvertisingIdClient advertisingIdClient;

  @NonNull
  private final Context context;

  @NonNull
  private final Executor executor;

  @NonNull
  private final AtomicReference<AdvertisingIdResult> resultRef = new AtomicReference<>();

  public AdvertisingInfo(
      @NonNull Context context,
      @NonNull Executor executor
  ) {
    this(context, executor, new SafeAdvertisingIdClient());
  }

  @VisibleForTesting
  AdvertisingInfo(
      @NonNull Context context,
      @NonNull Executor executor,
      @NonNull SafeAdvertisingIdClient advertisingIdClient
  ) {
    this.context = context;
    this.executor = executor;
    this.advertisingIdClient = advertisingIdClient;
  }

  public void prefetch() {
    getAdvertisingIdResult();
  }

  @Nullable
  public String getAdvertisingId() {
    return getAdvertisingIdResult().getId();
  }

  public boolean isLimitAdTrackingEnabled() {
    return getAdvertisingIdResult().isLimitAdTrackingEnabled();
  }

  private AdvertisingIdResult getAdvertisingIdResult() {
    AdvertisingIdResult advertisingIdResult = resultRef.get();
    if (advertisingIdResult == null) {
      // Multiple concurrent tasks are accepted. Only one commit is accepted then.
      if (isMainThread()) {
        executor.execute(new SafeRunnable() {
          @Override
          public void runSafely() {
            fetchResultOnWorkerThread();
          }
        });
      } else {
        fetchResultOnWorkerThread();
      }
    }

    advertisingIdResult = resultRef.get();
    if (advertisingIdResult == null) {
      return AdvertisingIdResult.defaultAdvertisingIdResult();
    } else {
      return advertisingIdResult;
    }
  }

  private boolean isMainThread() {
    Looper mainLooper = Looper.getMainLooper();
    if (mainLooper == null) {
      return false;
    }
    return Thread.currentThread().equals(mainLooper.getThread());
  }

  @WorkerThread
  private void fetchResultOnWorkerThread() {
    AdvertisingIdResult advertisingIdResult;

    try {
      String id = advertisingIdClient.getId(context);
      boolean limitAdTrackingEnabled = advertisingIdClient.isLimitAdTrackingEnabled(context);

      if (limitAdTrackingEnabled) {
        advertisingIdResult = AdvertisingIdResult.limitedAdvertisingIdResult();
      } else {
        advertisingIdResult = AdvertisingIdResult.unlimitedAdvertisingIdResult(id);
      }
    } catch (MissingPlayServicesAdsIdentifierException e) {
      // This cannot be fixed during the runtime. Let's cache the failure.
      advertisingIdResult = AdvertisingIdResult.defaultAdvertisingIdResult();
      logger.debug("Error getting advertising id", e);
    } catch (Exception e) {
      // Keep trying to get result on next try
      logger.debug("Error getting advertising id", e);
      return;
    }

    resultRef.compareAndSet(null, advertisingIdResult);
  }

  @VisibleForTesting
  static class SafeAdvertisingIdClient {

    @WorkerThread // Google API throws when getting the advertising ID on main thread because of potential deadlock.
    public String getId(@NonNull Context context) throws Exception {
      try {
        Info advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
        return advertisingIdInfo.getId();
      } catch (LinkageError e) {
        throw new MissingPlayServicesAdsIdentifierException(e);
      }
    }

    @WorkerThread // Google API throws when getting the advertising ID on main thread because of potential deadlock.
    public boolean isLimitAdTrackingEnabled(@NonNull Context context) throws Exception {
      try {
        Info advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
        return advertisingIdInfo.isLimitAdTrackingEnabled();
      } catch (LinkageError e) {
        throw new MissingPlayServicesAdsIdentifierException(e);
      }
    }
  }

  private static class AdvertisingIdResult {

    private static final AdvertisingIdResult DEFAULT_INSTANCE = new AdvertisingIdResult(null, false);
    private static final AdvertisingIdResult LIMITED_INSTANCE = new AdvertisingIdResult(
        "00000000-0000-0000-0000-000000000000",
        true
    );

    @Nullable
    private final String id;

    private final boolean isLimitAdTrackingEnabled;

    private AdvertisingIdResult(
        @Nullable String id,
        boolean isLimitAdTrackingEnabled
    ) {
      this.id = id;
      this.isLimitAdTrackingEnabled = isLimitAdTrackingEnabled;
    }

    static AdvertisingIdResult unlimitedAdvertisingIdResult(@NonNull String id) {
      return new AdvertisingIdResult(id, false);
    }

    static AdvertisingIdResult limitedAdvertisingIdResult() {
      return LIMITED_INSTANCE;
    }

    static AdvertisingIdResult defaultAdvertisingIdResult() {
      return DEFAULT_INSTANCE;
    }

    @Nullable
    public String getId() {
      return id;
    }

    public boolean isLimitAdTrackingEnabled() {
      return isLimitAdTrackingEnabled;
    }
  }

  static class MissingPlayServicesAdsIdentifierException extends Exception {

    MissingPlayServicesAdsIdentifierException(Throwable cause) {
      super("play-services-ads-identifier does not seems to be in the classpath", cause);
    }
  }
}
