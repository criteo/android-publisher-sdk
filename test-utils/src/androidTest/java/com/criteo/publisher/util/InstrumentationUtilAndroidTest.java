package com.criteo.publisher.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InstrumentationUtilAndroidTest {

  @Test
  public void isRunningInInstrumentationTest() {
    assertTrue(InstrumentationUtil.isRunningInInstrumentationTest());
  }

}