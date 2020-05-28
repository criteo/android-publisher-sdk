package com.criteo.publisher.model;

import androidx.annotation.NonNull;
import com.criteo.publisher.Clock;

/**
 * Token given to publisher so that he can asynchronously fetch an ad.
 * The ad asset is just a display URL (i.e. for banner and interstitial).
 */
public class DisplayUrlTokenValue extends AbstractTokenValue {

  @NonNull
  private final String displayUrl;

  public DisplayUrlTokenValue(
      @NonNull String displayUrl,
      @NonNull Slot slot,
      @NonNull Clock clock) {
    super(slot, clock);
    this.displayUrl = displayUrl;
  }

  @NonNull
  public String getDisplayUrl() {
    return displayUrl;
  }

}
