package com.criteo.publisher;

import android.os.SystemClock;

public class EpochClock implements Clock {

  @Override
  public long getCurrentTimeInMillis() {
    return System.currentTimeMillis();
  }
}
