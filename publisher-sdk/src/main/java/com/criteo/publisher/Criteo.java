package com.criteo.publisher;

import android.app.Application;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import java.util.List;

public abstract class Criteo {

  private static final String TAG = Criteo.class.getSimpleName();
  private static Criteo criteo;

  public static Criteo init(Application application, String criteoPublisherId, List<AdUnit> adUnits)
          throws CriteoInitException {
      synchronized (Criteo.class) {
          if (criteo == null) {
              try {
                DeviceUtil deviceUtil = DependencyProvider.getInstance()
                    .provideDeviceUtil(application.getApplicationContext());

                if (deviceUtil.isVersionSupported()) {
                  criteo = new CriteoInternal(application, adUnits, criteoPublisherId);
                } else {
                  criteo = new DummyCriteo();
                }
              } catch (IllegalArgumentException iae) {
                  throw iae;
              } catch (Throwable tr) {
                  Log.e(TAG, "Internal error initializing Criteo instance.", tr);
                  throw new CriteoInitException("Internal error initializing Criteo instance.", tr);
              }
          }
      }
      return criteo;
  }

  public static Criteo getInstance() {
      if (criteo == null) {
          throw new IllegalStateException("You must call Criteo.Init() before calling Criteo.getInstance()");
      }

      return criteo;
  }

  @VisibleForTesting
  static void setInstance(@Nullable Criteo instance) {
      criteo = instance;
  }

  public abstract void setBidsForAdUnit(Object object, AdUnit adUnit);

  @Nullable
  abstract Slot getBidForAdUnit(AdUnit adUnit);

  public abstract BidResponse getBidResponse(AdUnit adUnit);

  @Nullable
  abstract TokenValue getTokenValue(BidToken bidToken, AdUnitType adUnitType);

  abstract DeviceInfo getDeviceInfo();

  abstract Config getConfig();
}
