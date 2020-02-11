package com.criteo.publisher.model;

import android.support.annotation.NonNull;
import com.criteo.publisher.Clock;

public class TokenValue {

  private static final int SECOND_TO_MILLI = 1000;

  private final long tokenExpirationTime;

  @NonNull
  private final String displayUrl;

  @NonNull
  private final Clock clock;

  public TokenValue(
      long bidTime,
      int bidTtl,
      @NonNull String displayUrl,
      @NonNull Clock clock) {
    this.tokenExpirationTime = bidTime + bidTtl * SECOND_TO_MILLI;
    this.displayUrl = displayUrl;
    this.clock = clock;
  }

  public long gettokenExpirationTime() {
    return tokenExpirationTime;
  }

  @NonNull
  public String getDisplayUrl() {
    return displayUrl;
  }

  public boolean isExpired() {
    return gettokenExpirationTime() < clock.getCurrentTimeInMillis();
  }
}
