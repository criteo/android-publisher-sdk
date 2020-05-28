package com.criteo.publisher.headerbidding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Slot;

public interface HeaderBiddingHandler {

  boolean canHandle(@NonNull Object object);

  void enrichBid(@NonNull Object object, @Nullable AdUnit adUnit, @NonNull Slot slot);

}
