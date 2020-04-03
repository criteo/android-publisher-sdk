package com.criteo.publisher.util;

import junit.framework.Assert;
import org.junit.Test;

public class ReflectionUtilTest {

  @Test
  public void testCallMethodOnObjectWithNullParams() {
    Assert.assertNull(ReflectionUtil.callMethodOnObject(null, null, null));
  }

}