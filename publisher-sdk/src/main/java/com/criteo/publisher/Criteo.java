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

package com.criteo.publisher;

import static com.criteo.publisher.ErrorLogMessage.onUncaughtErrorAtPublicApi;
import static com.criteo.publisher.logging.DeprecationLogMessage.onDeprecatedMethodCalled;
import static com.criteo.publisher.SdkInitLogMessage.onDummySdkInitialized;
import static com.criteo.publisher.SdkInitLogMessage.onErrorDuringSdkInitialization;
import static com.criteo.publisher.SdkInitLogMessage.onSdkInitialized;
import static com.criteo.publisher.SdkInitLogMessage.onSdkInitializedMoreThanOnce;

import android.app.Application;
import android.util.Log;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.criteo.publisher.context.ContextData;
import com.criteo.publisher.context.UserData;
import com.criteo.publisher.interstitial.InterstitialActivityHelper;
import com.criteo.publisher.logging.Logger;
import com.criteo.publisher.logging.LoggerFactory;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.util.DeviceUtil;
import java.util.ArrayList;
import java.util.List;

@Keep
public abstract class Criteo {

  private static Criteo criteo;

  @Keep
  public static class Builder {

    @NonNull
    private final String criteoPublisherId;

    @NonNull
    private final Application application;

    @NonNull
    private List<AdUnit> adUnits = new ArrayList<>();

    @Nullable
    private Boolean usPrivacyOptOut;

    private boolean isDebugLogsEnabled = false;

    @Nullable
    private Boolean tagForChildDirectedTreatment = null;

    @Nullable
    private String inventoryGroupId = null;

    public Builder(@NonNull Application application, @NonNull String criteoPublisherId) {
      this.application = application;
      this.criteoPublisherId = criteoPublisherId;
    }

    public Builder adUnits(@Nullable List<AdUnit> adUnits) {
      if (adUnits == null) {
        this.adUnits = new ArrayList<>();
      } else {
        this.adUnits = adUnits;
      }
      return this;
    }

    public Builder usPrivacyOptOut(boolean usPrivacyOptOut) {
      this.usPrivacyOptOut = usPrivacyOptOut;
      return this;
    }

    @Deprecated
    public Builder mopubConsent(@Nullable String ignored) {
      Logger logger = LoggerFactory.getLogger(Builder.class);
      logger.log(onDeprecatedMethodCalled());
      return this;
    }

    public Builder debugLogsEnabled(boolean isDebugLogsEnabled) {
      this.isDebugLogsEnabled = isDebugLogsEnabled;
      return this;
    }

    /**
     * @see Criteo#setTagForChildDirectedTreatment(Boolean)
     */
    public Builder tagForChildDirectedTreatment(@Nullable Boolean tagForChildDirectedTreatment) {
      this.tagForChildDirectedTreatment = tagForChildDirectedTreatment;
      return this;
    }

    public Builder inventoryGroupId(@Nullable String inventoryGroupId) {
      this.inventoryGroupId = inventoryGroupId;
      return this;
    }

    public Criteo init() throws CriteoInitException {
      return Criteo.init(this);
    }
  }

  private static Criteo init(@NonNull Builder builder) throws CriteoInitException {
    Logger logger = LoggerFactory.getLogger(Criteo.class);

    synchronized (Criteo.class) {
      if (criteo == null) {
        try {
          DependencyProvider dependencyProvider = DependencyProvider.getInstance();
          dependencyProvider.setApplication(builder.application);
          dependencyProvider.setCriteoPublisherId(builder.criteoPublisherId);
          dependencyProvider.setInventoryGroupId(builder.inventoryGroupId);

          if (builder.isDebugLogsEnabled) {
            dependencyProvider.provideConsoleHandler().setMinLogLevel(Log.INFO);
          }

          DeviceUtil deviceUtil = dependencyProvider.provideDeviceUtil();
          if (deviceUtil.isVersionSupported()) {
            criteo = new CriteoInternal(
                builder.application,
                builder.adUnits,
                builder.usPrivacyOptOut,
                builder.tagForChildDirectedTreatment,
                dependencyProvider
            );

            logger.log(onSdkInitialized(builder.criteoPublisherId, builder.adUnits, getVersion()));
          } else {
            criteo = new DummyCriteo();

            logger.log(onDummySdkInitialized());
          }
        } catch(Throwable tr) {
          criteo = new DummyCriteo();

          CriteoInitException criteoInitException = new CriteoInitException(
              "Internal error initializing Criteo instance.",
              tr
          );
          logger.log(onErrorDuringSdkInitialization(criteoInitException));
          throw criteoInitException;
        }
      } else {
        logger.log(onSdkInitializedMoreThanOnce());
      }
      return criteo;
    }
  }

  public static Criteo getInstance() {
    if (criteo == null) {
      throw new CriteoNotInitializedException(
          "You must initialize the SDK before calling Criteo.getInstance()");
    }

    return criteo;
  }

  @VisibleForTesting
  static void setInstance(@Nullable Criteo instance) {
    criteo = instance;
  }

  public abstract void enrichAdObjectWithBid(Object object, @Nullable Bid bid);

  abstract void getBidForAdUnit(
      @Nullable AdUnit adUnit,
      @NonNull ContextData contextData,
      @NonNull BidListener bidListener
  );

  public void loadBid(
      @NonNull AdUnit adUnit,
      @NonNull BidResponseListener bidResponseListener
  ) {
    loadBid(adUnit, new ContextData(), bidResponseListener);
  }

  public abstract void loadBid(
      @NonNull AdUnit adUnit,
      @NonNull ContextData contextData,
      @NonNull BidResponseListener bidResponseListener
  );

  /**
   * Return the version of this SDK.
   */
  @NonNull
  public static String getVersion() {
    try {
      return DependencyProvider.getInstance().provideBuildConfigWrapper().getSdkVersion();
    } catch (Throwable t) {
      Logger logger = LoggerFactory.getLogger(Criteo.class);
      logger.log(onUncaughtErrorAtPublicApi(t));
      return "";
    }
  }

  @NonNull
  abstract DeviceInfo getDeviceInfo();

  @NonNull
  abstract Config getConfig();

  @NonNull
  abstract InterstitialActivityHelper getInterstitialActivityHelper();

  @NonNull
  public abstract CriteoBannerEventController createBannerController(@NonNull CriteoBannerAdWebView bannerView);

  public abstract void setUsPrivacyOptOut(boolean usPrivacyOptOut);

  @Deprecated
  public void setMopubConsent(@Nullable String ignored) {
    Logger logger = LoggerFactory.getLogger(Criteo.class);
    logger.log(onDeprecatedMethodCalled());
  }

  public abstract void setUserData(@NonNull UserData userData);

  /**
   * Sets COPPA tag to be sent with bid requests.
   * Refer to <a href="https://www.ftc.gov/business-guidance/privacy-security/childrens-privacy">gov website</a>
   * for more information about regulation.
   *
   * @param tagForChildDirectedTreatment
   * Pass {@code true} to indicate that your content is intended for children.
   * Pass {@code false} to indicate that it should not be treated as child-directed.
   * Pass {@code null} if you don't want to indicate how it should be treated.
   * If nothing is set it defaults to null.
   *
   * @see Criteo.Builder#tagForChildDirectedTreatment(Boolean) Set tag using Criteo Builder
   */
  public abstract void setTagForChildDirectedTreatment(@Nullable Boolean tagForChildDirectedTreatment);

}
