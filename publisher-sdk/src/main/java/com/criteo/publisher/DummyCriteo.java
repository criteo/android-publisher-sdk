package com.criteo.publisher;

import static com.criteo.publisher.Util.CompletableFuture.completedFuture;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.UserAgentCallback;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.DeviceInfo;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import java.util.concurrent.Future;

public class DummyCriteo extends Criteo {

  @Override
  public void setBidsForAdUnit(Object object, AdUnit adUnit) {
    // Nothing
  }

  @Nullable
  @Override
  Slot getBidForAdUnit(@Nullable AdUnit adUnit) {
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

  @NonNull
  @Override
  DeviceInfo getDeviceInfo() {
    return new DummyDeviceInfo();
  }

  @Override
  Config getConfig() {
    return new Config();
  }

  @Override
  public void setUsPrivacyOptOut(boolean usPrivacyOptOut) {
    // do nothing
  }

  private static class DummyDeviceInfo extends DeviceInfo {

    private DummyDeviceInfo() {
      super(null);
    }

    @NonNull
    @Override
    public Future<String> getUserAgent() {
      return completedFuture("");
    }

    @Override
    public void initialize(@NonNull UserAgentCallback userAgentCallback) {
      userAgentCallback.done();
    }

  }

}
