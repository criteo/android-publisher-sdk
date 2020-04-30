package com.criteo.publisher.model.nativeads;

import android.support.annotation.NonNull;
import com.criteo.publisher.Clock;
import com.criteo.publisher.model.AbstractTokenValue;
import com.criteo.publisher.model.Slot;

/**
 * Token given to publisher so that he can asynchronously fetch an native ad.
 */
public class NativeTokenValue extends AbstractTokenValue {

  @NonNull
  private final NativeAssets assets;

  public NativeTokenValue(
      @NonNull NativeAssets assets,
      @NonNull Slot slot,
      @NonNull Clock clock
  ) {
    super(slot, clock);
    this.assets = assets;
  }

  @NonNull
  public NativeAssets getNativeAssets() {
    return assets;
  }

}
