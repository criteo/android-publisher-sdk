package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import com.criteo.publisher.Clock;

public abstract class AbstractTokenValue {

  @NonNull
  private final Slot slot;

  @NonNull
  private final Clock clock;

  protected AbstractTokenValue(@NonNull Slot slot, @NonNull Clock clock) {
    this.slot = slot;
    this.clock = clock;
  }

  public boolean isExpired() {
    return slot.isExpired(clock);
  }

}
