package com.criteo.publisher.headerbidding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.BidManager;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.Slot;
import java.util.Map;

public class OtherAdServersHeaderBidding {

  private static final String CRT_CPM = "crt_cpm";
  private static final String CRT_DISPLAY_URL = "crt_displayUrl";
  private static final String CRT_SIZE = "crt_size";

  @NonNull
  private final BidManager bidManager;

  public OtherAdServersHeaderBidding(@NonNull BidManager bidManager) {
    this.bidManager = bidManager;
  }

  public boolean canHandle(@NonNull Object object) {
    return object instanceof Map;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void enrichBid(@NonNull Object object, @Nullable AdUnit adUnit) {
    if (!canHandle(object)) {
      return;
    }

    Slot slot = bidManager.getBidForAdUnitAndPrefetch(adUnit);
    if (slot == null) {
      return;
    }

    Map map = (Map) object;
    map.put(CRT_DISPLAY_URL, slot.getDisplayUrl());
    map.put(CRT_CPM, slot.getCpm());

    if (adUnit instanceof BannerAdUnit) {
      map.put(CRT_SIZE, slot.getWidth() + "x" + slot.getHeight());
    }
  }

}
