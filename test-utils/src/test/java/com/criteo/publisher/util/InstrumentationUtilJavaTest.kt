package com.criteo.publisher.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class InstrumentationUtilJavaTest {

  @Test
  fun isRunningInInstrumentationTest_GivenJavaTest_ReturnTrue() {
    assertThat(InstrumentationUtil.isRunningInInstrumentationTest()).isFalse()
  }

}