package com.criteo.publisher.cache;


import static org.junit.Assert.assertEquals;

import com.criteo.publisher.Util.LoggingUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EnvironmentTest {

  private LoggingUtil loggingUtil;

  @Before
  public void setUp() {
    loggingUtil = new LoggingUtil();
  }

  @Test
  @Ignore("Half baked feature that isn't used")
  public void getEnviorementVairableTest() {
    assertEquals(true, loggingUtil.isLoggingEnabled());
  }
}
