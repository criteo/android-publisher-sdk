package com.criteo.publisher.headerbidding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.BidManager;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Slot;
import java.util.List;

public class HeaderBidding {

  @NonNull
  private final BidManager bidManager;

  @NonNull
  private final List<HeaderBiddingHandler> handlers;

  public HeaderBidding(
      @NonNull BidManager bidManager,
      @NonNull List<HeaderBiddingHandler> handlers
  ) {
    this.bidManager = bidManager;
    this.handlers = handlers;
  }

  public void enrichBid(@Nullable Object object, @Nullable AdUnit adUnit) {
    if (object == null) {
      return;
    }

    for (HeaderBiddingHandler handler : handlers) {
      if (handler.canHandle(object)) {
        Slot slot = bidManager.getBidForAdUnitAndPrefetch(adUnit);
        if (slot == null) {
          return;
        }

        handler.enrichBid(object,adUnit, slot);
        return;
      }
    }
  }

}
