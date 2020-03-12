package com.criteo.publisher.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class CacheAdUnitTest {

  @Test
  public void equalsContract() throws Exception {
    EqualsVerifier.forClass(CacheAdUnit.class)
        .verify();
  }

}