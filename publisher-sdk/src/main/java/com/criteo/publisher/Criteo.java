package com.criteo.publisher;

import android.app.Application;
import android.util.Log;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.NativeAdUnit;
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
                  if (DeviceUtil.isVersionNotSupported()) {
                    criteo = new DummyCriteo();
                  } else {
                    criteo = new CriteoInternal(application, adUnits, criteoPublisherId);
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

  public abstract void setBidsForAdUnit(Object object, AdUnit adUnit);

  abstract Slot getBidForAdUnit(AdUnit adUnit);

  public abstract BidResponse getBidResponse(AdUnit adUnit);

  abstract TokenValue getTokenValue(BidToken bidToken, AdUnitType adUnitType);

  abstract DeviceInfo getDeviceInfo();

  public abstract void loadNativeAd(NativeAdUnit nativeAdUnit, CriteoNativeAdListener nativeAdListener);
}
