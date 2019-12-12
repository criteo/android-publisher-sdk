package com.criteo.publisher;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.UserAgentCallback;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.NativeAdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;

public class DummyCriteo extends Criteo {

  @Override
  public void setBidsForAdUnit(Object object, AdUnit adUnit) {
    // Nothing
  }

  @Nullable
  @Override
  Slot getBidForAdUnit(AdUnit adUnit) {
    return null;
  }

  @Override
  public BidResponse getBidResponse(AdUnit adUnit) {
    return new BidResponse();
  }

  @Nullable
  @Override
  TokenValue getTokenValue(BidToken bidToken, AdUnitType adUnitType) {
    return null;
  }

  @Override
  DeviceInfo getDeviceInfo() {
    return new DummyDeviceInfo();
  }

  @Override
  Config getConfig() {
    return new Config();
  }

  private static class DummyDeviceInfo extends DeviceInfo {

    @Override
    public String getUserAgent() {
      return "";
    }

    @Override
    public void initialize(@NonNull Context context, @NonNull UserAgentCallback userAgentCallback) {
      userAgentCallback.done();
    }

  }

}
