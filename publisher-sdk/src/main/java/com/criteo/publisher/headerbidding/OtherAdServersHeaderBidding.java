package com.criteo.publisher.headerbidding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.Slot;
import java.util.Map;

public class OtherAdServersHeaderBidding implements HeaderBiddingHandler {

  private static final String CRT_CPM = "crt_cpm";
  private static final String CRT_DISPLAY_URL = "crt_displayUrl";
  private static final String CRT_SIZE = "crt_size";

  @Override
  public boolean canHandle(@NonNull Object object) {
    return object instanceof Map;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void enrichBid(@NonNull Object object, @Nullable AdUnit adUnit, @NonNull Slot slot) {
    if (!canHandle(object)) {
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
