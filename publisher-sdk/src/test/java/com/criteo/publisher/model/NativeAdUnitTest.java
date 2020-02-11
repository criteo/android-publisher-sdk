package com.criteo.publisher.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class NativeAdUnitTest {

  @Test
  public void equalsContract() {
    EqualsVerifier.forClass(NativeAdUnit.class)
        .withRedefinedSuperclass()
        .verify();
  }

}