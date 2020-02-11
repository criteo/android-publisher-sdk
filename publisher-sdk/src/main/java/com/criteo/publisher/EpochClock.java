package com.criteo.publisher;

public class EpochClock implements Clock {

  @Override
  public long getCurrentTimeInMillis() {
    return System.currentTimeMillis();
  }
}
